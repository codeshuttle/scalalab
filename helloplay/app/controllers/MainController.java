package controllers;

import java.io.File;
import java.util.Map;

import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

public class MainController extends Controller {
    
//    public static Result index() {
//        return ok(views.html.index.render("Hello from Java"));
//    }
//    
//    public static Result addUser(){
//    	return register();
//    }
    
//    public static Result register(){
//		MultipartFormData body = request().body().asMultipartFormData();
//    	if (body!=null) {
//			FilePart file = body.getFile("avatar");
//			if (file != null) {
//				String fileName = file.getFilename();
//				String contentType = file.getContentType();
//				File avatar = file.getFile();
//				
//			}
//			Map<String, String[]> asFormUrlEncoded = request().body().asFormUrlEncoded();
//			return ok(views.html.register.render("Added "+asFormUrlEncoded.get("name")[0]+"!"));
//		}else{
//			return ok(views.html.register.render(""));
//		}
//    }
    
}
