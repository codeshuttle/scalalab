package controllers

import play.api.Routes
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import play.mvc.Http.MultipartFormData

case class Message(value: String)

object MessageController extends Controller {

  implicit val fooWrites = Json.writes[Message]

  
  def getMessage = Action {
    Ok(Json.toJson(Message("Hello from Scala")))
  }

  def javascriptRoutes = Action { implicit request =>
    Ok(Routes.javascriptRouter("jsRoutes")(
        routes.javascript.MessageController.getMessage,
        routes.javascript.UserController.addUser,
        routes.javascript.UserController.deleteUser,
        routes.javascript.UserController.allUsers,
        routes.javascript.UserController.updateUser
    )).as(JAVASCRIPT)
  }

//  
//  def addUser = Action {
//    val body = request().body().asMultipartFormData();
//	if (body!=null) {
//		val file = body.getFile("avatar");
//		if (file != null) {
//			val fileName = file.getFilename();
//			val contentType = file.getContentType();
//			val avatar = file.getFile();
//			
//		}
//		val asFormUrlEncoded = request().body().asFormUrlEncoded();
//		return ok(views.html.register.render("Added "+asFormUrlEncoded.get("name")+"!"));
//	}
//	return ok(views.html.register.render(""));
//  }
}