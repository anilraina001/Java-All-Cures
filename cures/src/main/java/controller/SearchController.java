package controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import dao.DiseaseANDConditionDaoImpl;

@RestController
@RequestMapping(path = "/isearch")
public class SearchController {

	@Autowired
	private DiseaseANDConditionDaoImpl diseaseANDconditionDaoImpl;

//	@RequestMapping(value = "/{article_id}", produces = "application/json", method = RequestMethod.GET)
//	public @ResponseBody Article getArticleDetails(@PathVariable int article_id, HttpServletRequest request) {
//		HttpServletRequest req = (HttpServletRequest) request;
//		HttpSession session = req.getSession(true);
//		/*
//		 * int reg_id = 0; if (session.getAttribute(Constant.USER) != null) {
//		 * Constant.log("#########USER IS IN SESSION########", 0); Registration user =
//		 * (Registration) session.getAttribute(Constant.USER); reg_id =
//		 * user.getRegistration_id(); System.out.println(reg_id); }
//		 */
//		return articleDaoImpl.getArticleDetails(article_id);
//
//	}
//
//	@RequestMapping(value = "/all", produces = "application/json", method = RequestMethod.GET)
//	public @ResponseBody ArrayList<Article> listArticlesAll() {
//		return diseaseANDconditionDaoImpl.getArticlesListAll();
//	}

	@RequestMapping(value = "/{search_string}", produces = "application/json", method = RequestMethod.GET)
	public @ResponseBody List listDataFromMatchingString(@PathVariable String search_string) {
		return diseaseANDconditionDaoImpl.getAllMatchingDCList(search_string);
	}

	@RequestMapping(value = "/hierarchy/{parent_id}", produces = "application/json", method = RequestMethod.GET)
	public @ResponseBody List listParentChildDiseaseCondtion(@PathVariable Integer parent_id) {
		return diseaseANDconditionDaoImpl.getParentChildDataDiseaseConditon(parent_id);
	}

//	@RequestMapping(value = "/{article_id}", produces = "application/json", method = RequestMethod.POST)
//	public @ResponseBody int updateArticle(@PathVariable int article_id, @RequestBody HashMap articleMap) {
//		return articleDaoImpl.updateArticleId(article_id, articleMap);
//	}
//
//	@RequestMapping(value = "/{article_id}", produces = "application/json", method = RequestMethod.DELETE)
//	public @ResponseBody int deleteArticle(@PathVariable int article_id) {
//		return articleDaoImpl.deleteArticleId(article_id);
//	}
//
//	@RequestMapping(value = "/readfile", produces = "application/json", method = RequestMethod.POST)
//	public @ResponseBody String readFile(@RequestBody HashMap filepath) {
//		String fp = (String) filepath.get("filepath");
//		return articleDaoImpl.readFile(fp);
//	}

}