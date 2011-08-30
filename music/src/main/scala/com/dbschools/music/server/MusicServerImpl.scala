package com.dbschools.music.server

import scala.collection.JavaConversions._
import java.io.Serializable
import java.lang.management.ManagementFactory
import java.rmi.RemoteException
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import java.rmi.server.UnicastRemoteObject
import java.util.ArrayList
import java.util.Arrays
import java.util.Collection
import java.util.Date
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Set
import java.util.TreeSet
import java.util.concurrent.ConcurrentHashMap
import javax.management.ObjectName
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger
import org.hibernate.FetchMode
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.criterion.Criterion
import org.hibernate.criterion.Restrictions
import com.dbschools.DatabaseAccessException
import com.dbschools.NoSuchUserPasswordException
import com.dbschools.music.ClientSession
import com.dbschools.music.Constants
import com.dbschools.music.GroupTerm
import com.dbschools.music.admin.ui.MusicianImportBatch
import com.dbschools.music.assess.SummaryRecord
import com.dbschools.music.assess.SummaryRecordDependency
import com.dbschools.music.decortrs.YearDecorator
import com.dbschools.music.events.Event
import com.dbschools.music.events.TypeCode
import com.dbschools.music.orm.Log
import com.dbschools.music.orm.Musician
import com.dbschools.music.orm.MusicianGroup
import com.dbschools.music.orm.Piece
import com.dbschools.music.orm.User
import java.math.BigInteger
import java.util.HashSet

/**
 * A server implementation for the music application.
 * @param databases a map of databases
 * @param rmiRegistryPort the port to use
 * @author David C. Briccetti
 */
class MusicServerImpl(databases: Map[String, SessionFactory], rmiRegistryPort: Int)
    extends UnicastRemoteObject with MusicServer with MusicServerImplMBean {

  def this(databases: Map[String, SessionFactory]) = this(databases, Registry.REGISTRY_PORT)

  private val logger = Logger.getLogger(classOf[MusicServerImpl])
  private val databaseInstances = new ConcurrentHashMap[String, DatabaseInstance]
  private val musicianImporter = new MusicianImporter
  private val clientSessions = new ConcurrentHashMap[java.lang.Integer, ClientSession]
  private var numLogins = 0
  private var numGroupAssignments = 0
  private var numAssessments = 0
  private var lastLoginDate: Date = null

  for (e <- databases.entrySet) {
    val databaseName = e.getKey
    val sessionFactory = e.getValue
    DefaultDataCreator.createIfEmpty(sessionFactory)
    databaseInstances.put(databaseName, new DatabaseInstance(sessionFactory, Helper.getMusicPieces(sessionFactory)))
    logger.debug("DatabaseInstance created for " + databaseName)
  }
  val rmiRegistry = LocateRegistry.getRegistry(rmiRegistryPort)
  logger.debug(rmiRegistry + "contains " + Arrays.asList(rmiRegistry.list))
  rmiRegistry.rebind(Constants.RMI_MUSIC_BIND_NAME, this)
  logger.info("MusicServer created and bound to registry")
  var mbs = ManagementFactory.getPlatformMBeanServer
  mbs.registerMBean(this, new ObjectName("com.dbschools:type=MusicServer"))

  def getMusician(sessionId: Int, musicianId: java.lang.Integer): Musician = {
    val clientSession: ClientSession = clientSessions.get(sessionId)
    val session = getHibernateSession(clientSession)
    var musician: Musician = session.load(classOf[Musician], musicianId).asInstanceOf[Musician]
    session.close
    musician
  }

  def getNextEvent(sessionId: Int): Event = {
    val clientSession: ClientSession = clientSessions.get(sessionId)
    try {
      logger.debug("Fetching event from " + clientSession)
      clientSession.dequeueEvent
    }
    catch {
      case e: InterruptedException => {
        throw new RemoteException(e.getMessage)
      }
    }
  }

  def getMusicians(sessionId: Int, currentYearOnly: Boolean): Collection[Musician] = {
    val clientSession: ClientSession = clientSessions.get(sessionId)
    val session = getHibernateSession(clientSession)
    Helper.getMusicians(currentYearOnly, session)
  }

  def getAssessmentInfo(sessionId: Int,
      musicianIds: Collection[java.lang.Integer]): Collection[Musician] = {
    val musicians: Collection[Musician] = new TreeSet[Musician]
    if (musicianIds != null && musicianIds.size > 0) {
      val clientSession = clientSessions.get(sessionId)
      val session = getHibernateSession(clientSession)
      val criteria = session.createCriteria(classOf[Musician])
      criteria.setFetchMode("assessments", FetchMode.JOIN)
      criteria.setFetchMode("rejections", FetchMode.JOIN)
      criteria.setFetchMode("musicianGroups", FetchMode.JOIN)
      criteria.add(Restrictions.in("id", musicianIds))
      musicians.addAll(criteria.list.asInstanceOf[List[Musician]])
      session.close
    }
    musicians
  }

  def getGroupTermMemberIdsMap(sessionId: Int): Map[GroupTerm, Set[java.lang.Integer]] = {
    val clientSession = clientSessions.get(sessionId)
    val session = getHibernateSession(clientSession)
    val criteria = session.createCriteria(classOf[MusicianGroup])
    val musicianGroups = criteria.list.asInstanceOf[List[MusicianGroup]]
    val groupTermMembersMap = new java.util.HashMap[GroupTerm, Set[java.lang.Integer]]
    for (mg <- musicianGroups) {
      val musicianIdsForGroupTerm = Helper.getMusicianIdsSetForGroupTerm(groupTermMembersMap,
        new GroupTerm(mg.getGroup.getId.intValue(), mg.getSchoolYear.intValue()))
      musicianIdsForGroupTerm.add(mg.getMusician.getId)
    }
    session.close
    groupTermMembersMap
  }

  def getEntities[T](sessionId: Int, entityClass: Class[T], criterionColl: Collection[Criterion]): Collection[T] = {
    val clientSession = clientSessions.get(sessionId)
    val session = getHibernateSession(clientSession)
    val criteria = session.createCriteria(entityClass)
    if (criterionColl != null) {
      for (criterion <- criterionColl) {
        criteria.add(criterion)
      }
    }
    val entities = criteria.list.asInstanceOf[Collection[T]]
    session.close
    entities
  }

  def getMusicPieces(sessionId: Int): Collection[Piece] = {
    val dbi = databaseInstances.get(clientSessions.get(sessionId).getDatabaseName)
    Helper.getMusicPieces(dbi.getSessionFactory)
  }

  def getSummaryRecords(sessionId: Int, musicianIdList: Collection[java.lang.Integer]): Collection[SummaryRecord] = {
    val clientSession = clientSessions.get(sessionId)
    val dbi = databaseInstances.get(clientSession.getDatabaseName)
    Helper.getSummaryRecords(dbi, musicianIdList)
  }

  def getSummaryRecords(sessionId: Int, groupIdList: Collection[java.lang.Integer], instrumentIdList: Collection[java.lang.Integer], currentTermOnly: Boolean): Collection[SummaryRecord] = {
    val clientSession = clientSessions.get(sessionId)
    val dbi = databaseInstances.get(clientSession.getDatabaseName)
    Helper.getSummaryRecords(dbi, Helper.musiciansMatchingGroupAndInstrument(dbi.getSessionFactory, groupIdList, instrumentIdList, currentTermOnly))
  }

  def getCommentCounts(sessionId: Int): Map[java.lang.Integer, java.lang.Integer] = {
    val clientSession = clientSessions.get(sessionId)
    val session = getHibernateSession(clientSession)
    val transaction = session.beginTransaction
    val list = session.createSQLQuery(
      "select predefinedcomments_comment_id, count(predefinedcomments_comment_id) " +
      "from assessment_tag group by predefinedcomments_comment_id").list.asInstanceOf[List[Array[AnyRef]]]
    var commentCounts = new HashMap[java.lang.Integer, java.lang.Integer]
    for (obj <- list) {
      commentCounts.put(obj(0).asInstanceOf[java.lang.Integer], (obj(1).asInstanceOf[BigInteger]).intValue)
    }
    transaction.commit()
    session.close
    commentCounts
  }

  private def notifyChangedSummaryRecord(dbi: DatabaseInstance, musicianId: java.lang.Integer) {
    val summaryRecords = Helper.getSummaryRecords(dbi, Arrays.asList(musicianId))
    val summaryRecord = summaryRecords.iterator.next
    enqueueForAllClients(new Event(TypeCode.UPDATE_OBJECT, summaryRecord))
  }

  private def enqueueForAllClients(event: Event) {
    clientSessions synchronized {
      for (cs <- clientSessions.values) {
        cs.enqueueEvent(event)
      }
    }
  }

  private def log(clientSession: ClientSession, session: Session, typeCode: TypeCode, details: String) {
    session.save(new Log(typeCode, clientSession.getUser.getLogin, details))
  }

  def saveMusicianMusicGroups(sessionId: Int, schoolYear: Int, musicianGroups: Collection[MusicianGroup]) {
    val clientSession = clientSessions.get(sessionId)
    if (musicianGroups.isEmpty) {
      return
    }
    val session = getHibernateSession(clientSession)
    val transaction = session.beginTransaction
    session.createQuery("delete MusicianGroup where musician_id in (:musicianIdList) and schoolYear = :schoolYear").
      setParameterList("musicianIdList", Helper.extractMusicianIds(musicianGroups)).
      setParameter("schoolYear", schoolYear).
      executeUpdate
    val typeCode = TypeCode.SAVE_MUSICAN_MUSIC_GROUP
    for (mmg <- musicianGroups) {
      val msg = mmg.getMusician.getId + " " + mmg.getMusician.getStudentId + " " +
        mmg.getMusician.getName + " put in " + new YearDecorator(mmg.getSchoolYear) + " " +
        mmg.getGroup.getName + " on " + mmg.getInstrument.getName
      logger.info(msg)
      session.save(mmg)
      log(clientSession, session, typeCode, msg)
      numGroupAssignments += 1
      enqueueForAllClients(new Event(typeCode, mmg))
    }
    transaction.commit()
    session.close
  }

  def saveNewMusicianAndMusicGroups(sessionId: Int, termId: Int, musician: Musician,
      allGroupsForThisMusician: Collection[MusicianGroup]) {
    saveObject(sessionId, musician)
    saveMusicianMusicGroups(sessionId, termId, allGroupsForThisMusician)
  }

  def saveObject(sessionId: Int, obj : AnyRef): Serializable = {
    val clientSession = clientSessions.get(sessionId)
    val dbi = databaseInstances.get(clientSession.getDatabaseName)
    val session = getHibernateSession(clientSession)
    val transaction = session.beginTransaction
    var key: Serializable = null
    if (obj.isInstanceOf[MusicianImportBatch]) {
      musicianImporter.importMusicians(obj.asInstanceOf[MusicianImportBatch], session)
    } else {
      key = session.save(obj)
    }
    val typeCode = TypeCode.SAVE_OBJECT
    log(clientSession, session, typeCode, obj.toString)
    transaction.commit()
    session.close
    enqueueForAllClients(new Event(typeCode, obj))
    processSummaryRecordDependency(obj, dbi)
    key
  }

  private def processSummaryRecordDependency(obj : AnyRef, dbi: DatabaseInstance) {
    if (obj.isInstanceOf[SummaryRecordDependency]) {
      notifyChangedSummaryRecord(dbi, (obj.asInstanceOf[SummaryRecordDependency]).getMusician.getId)
    }
  }

  def updateObject(sessionId: Int, obj : AnyRef) {
    val clientSession = clientSessions.get(sessionId)
    val dbi = databaseInstances.get(clientSession.getDatabaseName)
    val session = getHibernateSession(clientSession)
    var transaction = session.beginTransaction
    session.update(obj)
    val typeCode: TypeCode = TypeCode.UPDATE_OBJECT
    log(clientSession, session, typeCode, obj.toString)
    transaction.commit()
    session.close
    enqueueForAllClients(new Event(typeCode, obj))
    processSummaryRecordDependency(obj, dbi)
  }

  def deleteObject(sessionId: Int, obj : AnyRef) {
    val clientSession = clientSessions.get(sessionId)
    val dbi = databaseInstances.get(clientSession.getDatabaseName)
    val session = getHibernateSession(clientSession)
    val transaction = session.beginTransaction
    val typeCode = TypeCode.DELETE_OBJECT
    val invalidSRMusicians = new HashSet[java.lang.Integer]
    obj match {
      case objs: Iterable[AnyRef] =>
        for (elem <- objs) {
          elem match {
            case srd: SummaryRecordDependency => invalidSRMusicians.add(srd.getMusician.getId)
          }
          deleteElement(session, elem, clientSession, typeCode, dbi)
        }
      case _ =>
        deleteElement(session, obj, clientSession, typeCode, dbi)
        processSummaryRecordDependency(obj, dbi)
    }
    transaction.commit()
    invalidSRMusicians.foreach(notifyChangedSummaryRecord(dbi, _))
    session.close
  }

  private def deleteElement(session: Session, elem: AnyRef, clientSession: ClientSession, typeCode: TypeCode, dbi: DatabaseInstance): Unit = {
    session.delete(elem)
    log(clientSession, session, typeCode, elem.toString)
    enqueueForAllClients(new Event(typeCode, elem))
  }

  private def getHibernateSession(clientSession: ClientSession): Session = {
    databaseInstances.get(clientSession.getDatabaseName).getSessionFactory.openSession
  }

  def logIn(databaseName: String, userName: String, password: String): ClientSession = {
    logger.info("User " + userName + " logging in to " + databaseName)
    val dbi = databaseInstances.get(databaseName)
    val sessionFactory = dbi.getSessionFactory
    if (sessionFactory == null) {
      throw new DatabaseAccessException("Database " + databaseName + " not found")
    }
    val session = sessionFactory.openSession
    val userList = session.createCriteria(classOf[User]).add(Restrictions.eq("login", userName)).
        list.asInstanceOf[List[User]]
    try {
      if (userList.isEmpty || !StringUtils.equals(userList.get(0).getPassword, password)) {
        throw new NoSuchUserPasswordException
      }
      val clientSession = new ClientSession(userList.get(0), databaseName)
      clientSessions.put(clientSession.getSessionId, clientSession)
      numLogins += 1
      lastLoginDate = new Date
      var transaction = session.beginTransaction
      log(clientSession, session, TypeCode.LOGIN, null)
      transaction.commit()
      clientSession
    } finally {
      session.close
    }
  }

  def logOut(sessionId: Int) {
    val clientSession = clientSessions.get(sessionId)
    val session = getHibernateSession(clientSession)
    val transaction = session.beginTransaction
    log(clientSession, session, TypeCode.LOGOUT, null)
    transaction.commit()
    session.close
    clientSessions.remove(clientSession.getSessionId)
    logger.info("User " + clientSession.getUser.getLogin + " logging out")
  }

  def getSchoolYears(sessionId: Int): Collection[java.lang.Integer] = {
    val clientSession = clientSessions.get(sessionId)
    val session = getHibernateSession(clientSession)
    val terms = session.createQuery("select distinct schoolYear from MusicianGroup").
        list.asInstanceOf[Collection[java.lang.Integer]]
    session.close
    terms
  }

  def getNumLogins = numLogins

  def getLoggedInUserNames: java.util.Collection[String] = {
    val names = new ArrayList[String]
    for (cs <- clientSessions.values) {
      val user = cs.getUser
      names.add(user.getFirstName + " " + user.getLastName)
    }
    names
  }

  def getNumAssessments = numAssessments

  def getNumGroupAssignments = numGroupAssignments

  def getLastLoginDate = lastLoginDate

}

object MusicServerImpl {
  private val serialVersionUID: Long = -3517652103010873651L
}