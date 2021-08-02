package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import model.Article;
import util.HibernateUtil;

//1	active
//7	WorkInProgress
//@Component makes sure it is picked up by the ComponentScan (if it is in the right package). This allows @Autowired to work in other classes for instances of this class
@Component
public class DiseaseANDConditionDaoImpl {
	private static ArrayList list = new ArrayList();

	public static List getAllMatchingDCList(String search_str) {

		// creating seession factory object
		Session factory = HibernateUtil.buildSessionFactory();

		// creating session object
		Session session = factory;

		// creating transaction object
		Transaction trans = (Transaction) session.beginTransaction();

		Query query = session.createNativeQuery(
				"SELECT a.article_id,  a.title , a.friendly_name,  a.subheading, a.content_type, a.keywords,  a.window_title,  a.content_location \r\n"
						+ " ,a.authored_by, a.published_by, a.edited_by, a.copyright_id, a.disclaimer_id, a.create_date, a.published_date, a.pubstatus_id,"
						+ " a.language_id, a.disease_condition_id, a.country_id \r\n"
						+ " ,dc.dc_name , dc.parent_dc_id \r\n" 
						+ " FROM allcures_schema.article a \r\n" 
						+ "inner join disease_condition dc on a.disease_condition_id = dc.dc_id\r\n"
						+ "inner join languages l on a.language_id = l.language_id\r\n"
						+ "inner join countries c on c.countrycodeid = a.country_id\r\n" + "where (dc.dc_name like '%"
						+ search_str + "%' \r\n" + "or dc.dc_desc like '%" + search_str + "%'\r\n" + "or title  like '%"
						+ search_str + "%'\r\n" + "or friendly_name  like '%" + search_str + "%'\r\n"
						+ "or window_title  like '%" + search_str + "%'\r\n" + "or countryname like '%" + search_str
						+ "%'\r\n" + "or lang_name like '%" + search_str + "%'\r\n" + ")\r\n"
						+ "and pubstatus_id = 3 \r\n" + "\r\n" + "");
		// needs other condition too but unable to find correct column
		// ArrayList<Article> list = (ArrayList<Article>) query.getResultList();
		System.out.println("result list searched article count@@@@@@@@@@@@@" + search_str);
		List<Object[]> results = (List<Object[]>) query.getResultList();
		List hmFinal = new ArrayList();
		for (Object[] objects : results) {
			HashMap hm = new HashMap();
			int article_id = (int) objects[0];
			String title = (String) objects[1];
			String friendly_name = (String) objects[2];
			String subheading = (String) objects[3];
			String content_type = (String) objects[4];
			String keywords = (String) objects[5];
			String window_title = (String) objects[6];
			String content_location = (String) objects[7];
			int authored_by = (int) objects[8];
			int published_by = objects[9] != null ? (int) objects[9] : 0;
			int edited_by = (int) objects[10];
			int copyright_id = (int) objects[11];
			int disclaimer_id = (int) objects[12];
			java.sql.Date create_date = (java.sql.Date) objects[13];
			java.sql.Date published_date = (java.sql.Date) objects[14];
			int pubstatus_id = (int) objects[15];
			int language_id = (int) objects[16];
			int disease_condition_id = (int) objects[17];
			int country_id = (int) objects[18];
			String dc_name = (String) objects[19];
			int parent_dc_id = objects[20] != null ? (int) objects[20] : 0;


			hm.put("article_id", article_id);
			hm.put("title", title);
			hm.put("friendly_name", friendly_name);
			hm.put("subheading", subheading);
			hm.put("content_type", content_type);
			hm.put("keywords", keywords);
			hm.put("window_title", window_title);
			hm.put("content_location", content_location);
			hm.put("authored_by", authored_by);
			hm.put("published_by", published_by);
			hm.put("edited_by", edited_by);
			hm.put("copyright_id", copyright_id);
			hm.put("disclaimer_id", disclaimer_id);
			hm.put("create_date", create_date);
			hm.put("published_date", published_date);
			hm.put("pubstatus_id", pubstatus_id);
			hm.put("language_id", language_id);
			hm.put("disease_condition_id", disease_condition_id);
			hm.put("country_id", country_id);
			hm.put("dc_name", dc_name);
			hm.put("parent_dc_id", parent_dc_id);
			hmFinal.add(hm);
			System.out.println(hm);
		}
		session.close();

		return hmFinal;
	}

	public static List getParentChildDataDiseaseConditon(Integer parent_id) {

		// creating seession factory object
		Session factory = HibernateUtil.buildSessionFactory();

		// creating session object
		Session session = factory;

		// creating transaction object
		Transaction trans = (Transaction) session.beginTransaction();
		String logic_here = "";

		if (parent_id == null ||  parent_id == 0) {
			logic_here = " is null  " ;
		}else {
			logic_here = " = "+parent_id;
		}
		
		Query query = session.createNativeQuery(
				"with recursive cte (dc_id, dc_desc, is_disease, dc_name, dc_status, parent_dc_id) as (\r\n"
						+ "  select     p.dc_id, p.dc_desc, p.is_disease, p.dc_name, p.dc_status, p.parent_dc_id\r\n"
						+ "  from       disease_condition p\r\n" + "  where      p.parent_dc_id"
								+ logic_here + "\r\n"
						+ "  union all\r\n"
						+ "  select     q.dc_id, q.dc_desc, q.is_disease, q.dc_name, q.dc_status, q.parent_dc_id\r\n"
						+ "  from       disease_condition q\r\n" + "  inner join cte\r\n"
						+ "          on q.parent_dc_id = cte.dc_id\r\n" + "          \r\n" + ")\r\n"
						+ "select * from cte where dc_status is not null;");
		// needs other condition too but unable to find correct column
		// ArrayList<Article> list = (ArrayList<Article>) query.getResultList();
		System.out.println("result list searched hierarchy count@@@@@@@@@@@@@" + parent_id);
		List<Object[]> results = (List<Object[]>) query.getResultList();
		List hmFinal = new ArrayList();
		for (Object[] objects : results) {
			HashMap hm = new HashMap();
			int dc_id = (int) objects[0];
			String dc_desc = (String) objects[1];
			int is_disease = (int) objects[2];
			String dc_name = (String) objects[3];
			int dc_status = (int) objects[4];
			int parent_dc_id = objects[5] != null ? (int) objects[5] : 0;

			hm.put("dc_id", dc_id);
			hm.put("dc_desc", dc_desc);
			hm.put("is_disease", is_disease);
			hm.put("dc_name", dc_name);
			hm.put("dc_status", dc_status);
			hm.put("parent_dc_id", parent_dc_id);
			hmFinal.add(hm);
			System.out.println(hm);
		}
		session.close();

		return hmFinal;
	}

}