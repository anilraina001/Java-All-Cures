package dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import controller.UserController;
import model.Article_dc_name;
import model.Registration;
import util.ArticleUtils;
import util.Constant;
import util.HibernateUtil;
import util.SendEmailUtil;

@Component
public class RegistrationDaoImpl {

	public Registration saveRegistration(String f_name, String l_name, String pwd, String email, Boolean accept,
			Integer type, Boolean policy, Integer state, Integer rem) {
		// creating seession factory object

		Registration user = null;
		Session factory = HibernateUtil.buildSessionFactory();

		// creating session object
		Session session = factory;

		Constant.log("Registering User with Firstname to DB:" + f_name, 0);
		/*
		 * TODO: Fix This Registration Logic. This is how its supposed to work For Any
		 * New User (Doctor or Patient) trying to register, Match the Email Address
		 * Provided during registration with the Email Address that we have on file for
		 * that doctor/patient If this is a doctor Create a new entry in the
		 * registration table with email, password, fname, lname etc. Use the
		 * RegistrationID (auto-increment) from the Registration Table to be the docid
		 * in the Doctors table use the first name, last name etc. from the signup page
		 * and update the doctors table with these 3 fields: docid = registrationid,
		 * firstname, last name
		 * 
		 * If this is a patient Create a new entry in the registration table with email,
		 * password, fname, lname etc. Use the RegistrationID (auto-increment) from the
		 * Registration Table to be the patientid in the Patient table use the first
		 * name, last name etc. from the signup page and update the patient table with
		 * these 3 fields: patientid = registrationid, firstname, last name
		 * 
		 * If we were to ever invite a doctor (or consumer) to register, it would have
		 * to be based purely on email addresses as we know that we will not have a
		 * docid (or patientid) till someone registers
		 * 
		 * 
		 */
		try {
			session.getTransaction().begin();

			Registration reg = new Registration();

			DoctorsDaoImpl doctor;
			PatientDaoImpl patient;
			// doctor.saveDoctors(f_name, l_name, email);
			// Integer docid = doctor.findAllDoctorsBydocid(email, f_name, l_name);
			// System.out.println("$$$$$$$$$$$$$$$$$$$$"+docid);

			// Create a New Row in the Registration Table

			reg.setEmail_address(email);
			reg.setPass_word(pwd);
			reg.setFirst_name(f_name);
			reg.setLast_name(l_name);
			reg.setAcceptance_condition(accept);
			reg.setRegistration_type(type);
			reg.setprivacy_policy(policy);
			reg.setAccount_state(state);
			reg.setRemember_me(rem);
			session.save(reg);
			session.getTransaction().commit();
			// session.close();
			// LOGIC Q: Right now we are setting the registration id as the docid for the
			// doctors table or patientid for the patient table
			// What will happen when we do mass doctor updates; There is a possibility that
			// the doctors created as a result that are
			// not registered with us might have a docid issued that is not the same as the
			// registration id; Unless we make the docid
			// and patientid in the doctors and patients table respectively a non-required
			// field and only once registration is done do
			// you get assigned an id (docid or patientid) and till then do not have to
			// carry it; Makes a lot of sense to do it that
			// way
			// TODO: Make DocId in Doctors table and PatientId in Patients table a non
			// required field and also foreign keyed into the Registration
			// table.
			// Create a Row in either the Doctors or Patients Table with the Same RegId

			if (type == 1) {
				Constant.log("Registering User is a Doctor", 0);
				RegistrationDaoImpl registerDao = new RegistrationDaoImpl();
				user = registerDao.findUserByEmail(email);
				doctor = new DoctorsDaoImpl();
				doctor.saveDoctors(user.getRegistration_id(), f_name, l_name, email);
				// sessionFactory.close();
				// Now that the doctor is signed up, should we log her in as well?
				// TODO: LogUserIn
			} else {
				Constant.log("Registering User is a Patient", 0);
				RegistrationDaoImpl registerDao = new RegistrationDaoImpl();
				user = registerDao.findUserByEmail(email);
				patient = new PatientDaoImpl();
				patient.savePatient(user.getRegistration_id(), f_name, l_name, email);
				// sessionFactory.close();
				// TODO: LogUserIn
			}
			SendEmailUtil.shootEmail(email, "Welcome Allcures",
					"Hi " + f_name + "," + " Thanks for the registration with allcures.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			session.getTransaction().rollback();
		}
		return user;
	}

//Used for Login Lookup
	public static Registration findAllUsers(String email, String pwd) {
		// creating seession factory object
		Session factory = HibernateUtil.buildSessionFactory();
		// creating session object
		Session session = factory;
		// Only Logging Password in Logs in Debug Mode
		Constant.log("Finding users with email:" + email + " and pass: " + pwd, 0);

		// creating transaction object
		Transaction trans = (Transaction) session.beginTransaction();
		Registration register = null;
		Query query = session
				.createNativeQuery("select registration_id, first_name, last_name, email_address, pass_word, "
						+ "registration_type, acceptance_condition, privacy_policy, account_state, remember_me, login_attempt,last_login_datatime from registration "
						+ "where email_address='" + email + "' and pass_word='" + pwd + "'");

		ArrayList<Registration> regList = (ArrayList<Registration>) query.getResultList();
		Constant.log(">>>>>>>>>>>>>>>>>>User Found for Email:" + email, 1);
		Iterator itr = regList.iterator();
		if (itr.hasNext()) {
			register = new Registration();
			Constant.log(">>>>>>>>>>>>>>>>>>User Found for Email:" + email, 1);
			Object[] obj = (Object[]) itr.next();
			{
				register.setRegistration_id(obj[0] != null ? (Integer) obj[0] : -1);
				register.setFirst_name(obj[1] != null ? (String) obj[1] : "");
				register.setLast_name((String) obj[2] != null ? (String) obj[2] : "");
				register.setEmail_address((String) obj[3] != null ? (String) obj[3] : "");
				// Security Best Practice: Do not put password in the user obj in session
				// String password = EnDeCryptor.decrypt((String)obj[4], secretKey);
				// register.setPass_word((String)password);
				register.setRegistration_type(obj[5] != null ? (Integer) obj[5] : 1);
				register.setAcceptance_condition(obj[6] != null ? (Boolean) obj[6] : false);
				register.setprivacy_policy(obj[7] != null ? (Boolean) obj[7] : false);
				register.setAccount_state(obj[8] != null ? (Integer) obj[8] : 3);
				register.setRemember_me(obj[9] != null ? (Integer) obj[9] : 0);
				register.setLogin_attempt(obj[10] != null ? (Integer) obj[10] : 0);
				register.setLast_login_datatime((java.util.Date) obj[11]);
				Constant.log(Constant.PREFIX + obj[0], 0);
				Constant.log(Constant.FIRST_NAME + obj[1], 0);
			}
		}
		session.close();
		return register;
	}

	public static Registration findUserByEmail(String email) {
		// creating seession factory object
		Session factory = HibernateUtil.buildSessionFactory();

		Session session = factory;

		// creating transaction object
		Transaction trans = (Transaction) session.beginTransaction();
		Constant.log((">>>>>>>>>>>>>>>>>>" + email), 0);
		int docid = 0;

		Registration register = null;
		Query query = session
				.createNativeQuery("select * from registration where email_address='" + email.trim() + "'");
		ArrayList<Registration> list = (ArrayList<Registration>) query.getResultList();
		Iterator itr = list.iterator();
		if (itr.hasNext()) {
			register = new Registration();
			Constant.log(">>>>>>>>>>>>>>>>>>User Found for Email:" + email, 1);
			Object[] obj = (Object[]) itr.next();
			{
				register.setRegistration_id(obj[0] != null ? (Integer) obj[0] : -1);
				register.setFirst_name(obj[1] != null ? (String) obj[1] : "");
				register.setLast_name((String) obj[2] != null ? (String) obj[2] : "");
				register.setEmail_address((String) obj[3] != null ? (String) obj[3] : "");
				// Security Best Practice: Do not put password in the user obj in session
				// String password = EnDeCryptor.decrypt((String)obj[4], secretKey);
				// register.setPass_word((String)password);
				register.setRegistration_type(obj[5] != null ? (Integer) obj[5] : 1);
				register.setAcceptance_condition(obj[6] != null ? (Boolean) obj[6] : false);
				register.setprivacy_policy(obj[7] != null ? (Boolean) obj[7] : false);
				register.setAccount_state(obj[8] != null ? (Integer) obj[8] : 3);
				register.setRemember_me(obj[9] != null ? (Integer) obj[9] : 0);
				register.setLast_login_datatime((Timestamp) obj[10]);
				register.setLogin_attempt(obj[11] != null ? (Integer) obj[11] : 0);
				Constant.log(Constant.PREFIX + obj[0], 0);
				Constant.log(Constant.FIRST_NAME + obj[1], 0);
			}
		}
		session.close();
		return register;
	}

	public static Registration findUserByRegId(int regid) {
		// creating seession factory object
		Session factory = HibernateUtil.buildSessionFactory();

		Session session = factory;

		// creating transaction object
		Transaction trans = (Transaction) session.beginTransaction();
		Constant.log((">>>>>>>>>>>>>>>>>>FINDING USER FOR ID:" + regid), 0);
		int docid = 0;

		Registration register = null;
		Query query = session.createNativeQuery("select * from registration where registration_id=" + regid);
		ArrayList<Registration> list = (ArrayList<Registration>) query.getResultList();
		Iterator itr = list.iterator();
		if (itr.hasNext()) {
			register = new Registration();
			Constant.log(">>>>>>>>>>>>>>>>>>User Found for ID:" + regid, 1);
			Object[] obj = (Object[]) itr.next();
			{
				register.setRegistration_id(obj[0] != null ? (Integer) obj[0] : -1);
				register.setFirst_name(obj[1] != null ? (String) obj[1] : "");
				register.setLast_name((String) obj[2] != null ? (String) obj[2] : "");
				register.setEmail_address((String) obj[3] != null ? (String) obj[3] : "");
				// Security Best Practice: Do not put password in the user obj in session
				// String password = EnDeCryptor.decrypt((String)obj[4], secretKey);
				// register.setPass_word((String)password);
				register.setRegistration_type(obj[5] != null ? (Integer) obj[5] : 1);
				register.setAcceptance_condition(obj[6] != null ? (Boolean) obj[6] : false);
				register.setprivacy_policy(obj[7] != null ? (Boolean) obj[7] : false);
				register.setAccount_state(obj[8] != null ? (Integer) obj[8] : 3);
				register.setRemember_me(obj[9] != null ? (Integer) obj[9] : 0);
				Constant.log(Constant.PREFIX + obj[0], 0);
				Constant.log(Constant.FIRST_NAME + obj[1], 0);
			}
		}
		session.close();
		return register;
	}

	public String updatePassword(String password, String email) {
		// creating seession factory object
		Session factory = HibernateUtil.buildSessionFactory();
		// creating session object
		Session session = factory;
		// creating transaction object
		Transaction trans = (Transaction) session.beginTransaction();
		// Query queryApproved = session.createNativeQuery("UPDATE registration SET
		// pass_word= '"+ password+"' where registration_id = "+reg_id+" );");

		int ret = 0;
		try {
			Query checkEmailExists = session.createNativeQuery(
					"select email_address from  registration where email_address = '" + email + "' ;");

			List<Object[]> results = (List<Object[]>) checkEmailExists.getResultList();
			System.out.println("result list Email Check@@@@@@@@@@@@@ size=" + results.size());
			if (results.size() == 0) {
				return "Sorry, the email address you entered does not exist in our database.";
			}

			System.out.println("check email exists in  registration table for email passed from UI =  " + email);

			Query queryApproved = session.createNativeQuery(
					"UPDATE registration SET pass_word= '" + password + "' where email_address = '" + email + "' ;");

			ret = queryApproved.executeUpdate();
			trans.commit();
			System.out.println("updated registration table password for email =  " + email);

		} catch (Exception ex) {
			trans.rollback();
		} finally {
			session.close();
		}

		return ret + "";
	}

	public int checkEmail(String email) {
		// creating seession factory object
		Session factory = HibernateUtil.buildSessionFactory();
		// creating session object
		Session session = factory;
		// creating transaction object
		Transaction trans = (Transaction) session.beginTransaction();
		// Query queryApproved = session.createNativeQuery("UPDATE registration SET
		// pass_word= '"+ password+"' where registration_id = "+reg_id+" );");

		int ret = 0;
		try {
			Query checkEmailExists = session.createNativeQuery(
					"select email_address from  registration where email_address = '" + email + "' ;");

			List<Object[]> results = (List<Object[]>) checkEmailExists.getResultList();
			System.out.println("result list Email Check@@@@@@@@@@@@@ size=" + results.size());
			if (results.size() == 0) {
				return 0;
			} else {
				String encEmail = new UserController().getEmailEncrypted(email);
				String link = "http://localhost:3000/loginForm/ResetPass/?em=" + encEmail;
				SendEmailUtil.shootEmail(email, "Test subject", "Password reset link here...\n" + link);
				return 1;
			}
//			System.out.println("check email exists in  registration table for email passed from UI =  " + email);

		} catch (Exception ex) {
			trans.rollback();
		} finally {
			session.close();
		}

		return ret;
	}

	public static int updateLoginDetails(String email) {

		// creating seession factory object
		Session factory = HibernateUtil.buildSessionFactory();

		// creating session object
		Session session = factory;
		// creating transaction object
		Transaction trans = (Transaction) session.beginTransaction();

		java.sql.Timestamp last_login_datetime = new java.sql.Timestamp(new java.util.Date().getTime());
		Query query = session.createNativeQuery(
				"update registration  as dest , (\r\n" + " SELECT (case when login_attempt IS NULL then 1\r\n"
						+ "             else login_attempt + 1\r\n" + "        end) as login_attempt\r\n"
						+ " FROM registration\r\n" + " WHERE email_address = '" + email + "'\r\n" + " ) as src\r\n"
						+ " set dest.login_attempt = src.login_attempt\r\n" + " , dest.last_login_datetime = '"
						+ last_login_datetime + "'\r\n" + " where dest.email_address ='" + email + "';");
		// needs other condition too but unable to find correct column
		int ret = 0;
		try {
			ret = query.executeUpdate();
			trans.commit();
			System.out.println("updated registration table for email_address =  " + email);

		} catch (Exception ex) {
			trans.rollback();
		} finally {
			// session.close();
			session.close();
		}
		// session.close();

		return ret;
	}

	public static int resetLoginDetails(int regId) {

		// creating seession factory object
		Session factory = HibernateUtil.buildSessionFactory();

		// creating session object
		Session session = factory;
		// creating transaction object
		Transaction trans = (Transaction) session.beginTransaction();

		java.sql.Timestamp last_login_datetime = new java.sql.Timestamp(new java.util.Date().getTime());
		Query query = session
				.createNativeQuery("update registration set login_attempt = 0 \r\n" + " , last_login_datetime = '"
						+ last_login_datetime + "'\r\n" + " where registration_id =" + regId + ";");
		// needs other condition too but unable to find correct column
		int ret = 0;
		try {
			ret = query.executeUpdate();
			trans.commit();
			System.out.println("reset registration table for reg_id =  " + regId);

		} catch (Exception ex) {
			trans.rollback();
		} finally {
			// session.close();
			session.close();
		}
		// session.close();

		return ret;
	}

	public static int subscribe(long mobile, HashMap ns_map) {
		// creating seession factory object
		Session factory = HibernateUtil.buildSessionFactory();

		// creating session object
		Session session = factory;
		// creating transaction object
		Transaction trans = (Transaction) session.beginTransaction();

		java.util.Date date = new java.util.Date();
		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		String nl_start_date = sqlDate.toString();

		int nl_subscription_disease_id = (int) ns_map.get("nl_subscription_disease_id");
		int nl_sub_type = (int) ns_map.get("nl_sub_type");
		int nl_subscription_cures_id = (int) ns_map.get("nl_subscription_cures_id");
		System.out.println("Subscribe create_date>>>>>" + nl_start_date);
		// set active =1 for new subscription
		Query query = session.createNativeQuery("INSERT INTO `allcures_schema`.`newsletter` (\r\n" + "\r\n"
				+ "`nl_subscription_disease_id`,\r\n" + "`nl_start_date`,\r\n" + "`nl_sub_type`,\r\n" + "`mobile`,\r\n"
				+ "`nl_subscription_cures_id`, `active`)\r\n" + " VALUES \r\n" + "(" + "\r\n"
				+ nl_subscription_disease_id + ",\r\n '" + nl_start_date + "' ,\r\n" + nl_sub_type + ",\r\n" + mobile
				+ ",\r\n" + nl_subscription_cures_id + ",1);\r\n");
		// needs other condition too but unable to find correct column
		System.out.println(query);
		int ret = 0;
		try {
			ret = query.executeUpdate();
			trans.commit();
			System.out.println("inserted new entry to newsletter table for mobile =  " + mobile);

		} catch (Exception ex) {
			trans.rollback();
		} finally {
			// session.close();
			session.close();
		}
		// session.close();

		return ret;
	}

	public static int updatesubscribe(long mobile, HashMap ns_map) {
		// creating seession factory object
		Session factory = HibernateUtil.buildSessionFactory();
		// creating session object
		Session session = factory;
		// creating transaction object
		Transaction trans = (Transaction) session.beginTransaction();
		/*
		 * HttpServletRequest request = ((ServletRequestAttributes)
		 * RequestContextHolder.currentRequestAttributes()) .getRequest(); HttpSession
		 * sessionreq = request.getSession(true);
		 * 
		 * int reg_id = 0; if (sessionreq.getAttribute(Constant.USER) != null) {
		 * Constant.log("update subscription #########USER IS IN SESSION########", 0);
		 * Registration user = (Registration) sessionreq.getAttribute(Constant.USER);
		 * reg_id = user.getRegistration_id(); System.out.println(reg_id); // to =
		 * user.getEmail_address(); } System.out.println(reg_id);
		 */

		int nl_subscription_disease_id = (int) ns_map.get("nl_subscription_disease_id");
		int nl_sub_type = (int) ns_map.get("nl_sub_type");
		int nl_subscription_cures_id = (int) ns_map.get("nl_subscription_cures_id");

		String updateStr = "";
//		if ((mobile + "").equals("")) {
//			updateStr += " mobile=" + mobile + ",";
//		}
		if (!(nl_subscription_disease_id + "").equals("")) {
			updateStr += " nl_subscription_disease_id=" + nl_subscription_disease_id + ",";
		}
		if (!(nl_sub_type + "").equals("")) {
			updateStr += " nl_sub_type=" + nl_sub_type + ",";
		}
		if (!(nl_subscription_cures_id + "").equals("")) {
			updateStr += " nl_subscription_cures_id=" + nl_subscription_cures_id + ",";
		}

		updateStr = updateStr.replaceAll(",$", "");

		Query queryArticlePromoPaid = session
				.createNativeQuery("UPDATE newsletter SET " + updateStr + "  WHERE mobile = " + mobile + ";");

		int ret = 0;
		try {
			ret = queryArticlePromoPaid.executeUpdate();
			trans.commit();
			System.out.println("updated newsletter table for mobile  =  " + mobile);
//			SendEmailUtil.shootEmail(null, "updated scbscription ",
//					"Hi, \n\r updated newsletter table for mobile  =  " + mobile);

		} catch (Exception ex) {
			trans.rollback();
		} finally {
			session.close();
		}

		return ret;
	}

	public int unsubscribe(long mobile) {
		// creating seession factory object
		Session factory = HibernateUtil.buildSessionFactory();
		// creating session object
		Session session = factory;
		// creating transaction object
		Transaction trans = (Transaction) session.beginTransaction();
//		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
//				.getRequest();
//		HttpSession sessionreq = request.getSession(true);
//
//		int reg_id = 0;
//		if (sessionreq.getAttribute(Constant.USER) != null) {
//			Constant.log("update subscription #########USER IS IN SESSION########", 0);
//			Registration user = (Registration) sessionreq.getAttribute(Constant.USER);
//			reg_id = user.getRegistration_id();
//			System.out.println(reg_id);
//			// to = user.getEmail_address();
//		}
//		System.out.println(reg_id);

		java.util.Date date = new java.util.Date();
		java.sql.Date sqlDate = new java.sql.Date(date.getTime());
		String nl_end_date = sqlDate.toString();
		
		Query queryArticlePromoPaid = session.createNativeQuery(
				"UPDATE newsletter SET active=0  and nl_end_date = '"+nl_end_date+"' WHERE mobile=" + mobile + ");");

		int ret = 0;
		try {
			ret = queryArticlePromoPaid.executeUpdate();
			trans.commit();
			System.out.println("unscribe newsletter table for mobile = " + mobile);
//			SendEmailUtil.shootEmail(null, "Unscribed allcures ",
//					"Hi, \n\r updated newsletter table for reg_id  =  " + reg_id);

		} catch (Exception ex) {
			trans.rollback();
		} finally {
			session.close();
		}

		return ret;
	}

}
