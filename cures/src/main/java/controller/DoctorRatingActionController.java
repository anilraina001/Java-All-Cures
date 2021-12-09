package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.DoctorsratingDaoImpl;
import dao.RatingDaoImpl;
import util.Constant;

/**
 * Servlet implementation class DoctorRatingActionController TODO: Add Exception
 * Handling TODO: Every API must return some status code to the front end and an
 * error code and error msg if unsuccessful
 */
public class DoctorRatingActionController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DoctorRatingActionController() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		Constant.log("In Rating Action Controller", 0);
		String cmd = request.getParameter("cmd");
		if (cmd != null && cmd.equals("rateAsset")) {
			int ret = rateAssetReq(request);
			response.setStatus(200);
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			Constant.log("1/0 Response Requested got >"+ret, 1);
			PrintWriter out = response.getWriter();
			if (ret !=1) ret = 0;
			out.write(""+ret);
			out.flush();
			Constant.log("Responding with response =" + ret, 1);
		} else if (cmd != null && cmd.equals("getTopRatedAssets")) {
			// get the AssetType (Doctors or Articles or Medicine or Whatever
			// get How Many e.g. Top 5 or Top 10
			// Return these Assets as JSON
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	public int rateAsset(String comments, int ratedBy, int target, int targetType, int ratedByType, float rating) {
		System.out.println("rating" + rating);
		int value = 0;
		RatingDaoImpl ratingDao = new RatingDaoImpl();
		if (null != comments && comments.trim().length() > 0) {
			System.out.println("rating created for comments");
			DoctorsratingDaoImpl docrating = new DoctorsratingDaoImpl();
			value = docrating.saveRating(comments, ratedBy, ratedByType, target, targetType, rating);
			return value;
		}
		List listOfRatings = ratingDao.findRatingByIdandTypeandRatedByandRatedByType(target, targetType, ratedBy,
				ratedByType);
		if (null != listOfRatings && listOfRatings.size() > 0) {
			System.out.println("rating updated");
			value = ratingDao.updateRatingCommentsCombined(target, targetType, ratedBy, ratedByType, rating, comments);
		} else {
			System.out.println("rating created");
			DoctorsratingDaoImpl docrating = new DoctorsratingDaoImpl();
			value = docrating.saveRating(comments, ratedBy, ratedByType, target, targetType, rating);
		}
		return value;
	}

	public int rateAssetReq(HttpServletRequest request) {
		String comments = (String) request.getParameter("comments");
		String ratedbyid = (String) request.getParameter("ratedbyid");
		String ratedbytype = (String) request.getParameter("ratedbytype");
		String targetid = (String) request.getParameter("targetid");
		String targetType = (String) request.getParameter("targetTypeid");
		String ratingval = (String) request.getParameter("ratingVal");
		System.out.println("ratingVal" + ratingval);
		int targetTypeid = 0;
		int ratebyTypeid = 0;
		if (targetType != null && (targetType.equals("doctor") || "1".equalsIgnoreCase(targetType))) {
			targetTypeid = 1;
		} else if (targetType != null && (targetType.equals("article") || "2".equalsIgnoreCase(targetType))) {
			targetTypeid = 2;
		} else if (targetType != null && (targetType.equals("medicine") || "3".equalsIgnoreCase(targetType))) {
			targetTypeid = 3;
		}

		if (ratedbytype != null && (ratedbytype.equals("doctor") || "1".equalsIgnoreCase(ratedbytype))) {
			ratebyTypeid = 1;
		} else if (ratedbytype != null && (ratedbytype.equals("patient") || "2".equalsIgnoreCase(targetType))) {
			ratebyTypeid = 2;
		}
		Float rv = 0.0f;
		if (ratingval != null)
			rv = Float.parseFloat(ratingval);
		System.out.println("ratingval 1" + ratingval);
		return rateAsset(comments, Integer.parseInt(ratedbyid), Integer.parseInt(targetid), targetTypeid, ratebyTypeid,
				rv);
	}

}
