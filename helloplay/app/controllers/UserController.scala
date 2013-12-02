package controllers

import play.api.data.format.Formats._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.mvc.Controller
import models.User
import play.api.libs.json.Json

object UserController extends Controller {
	implicit val userWrites = Json.writes[User]
  
	val userForm = Form(
    mapping(
        "id" -> of[Long],
      "name" -> text,
      "password" -> text,
      "email" -> email)(User.apply)(User.unapply) )
  
   private def validateForm(form: Form[User]) = {
    if( !(form("name").value.get isEmpty()) && User.exists(form("name").value.get)){
      form.withGlobalError("User("+form("name").value.get+") Already exists!")
    } else if ( !(form("email").value.get isEmpty()) && !(form("password").value.get isEmpty())
        && !(form("name").value.get isEmpty())    
    ) {
      form
    } else{
      form.withGlobalError("Invalid userid/password!")
    }
  }
      
  def addUser = Action { implicit request =>
    userForm.bindFromRequest.fold({
      formWithErrors => BadRequest(formWithErrors.globalErrors.map(f => f.message).reduce(_+","+_) )
    }, { user =>
      val theForm = validateForm(userForm.fill(user))
      if (theForm.hasErrors) {
        BadRequest(theForm.globalErrors.map(f => f.message).reduce(_+","+_))
      } else {
        User.create(theForm("name").value.get, theForm("email").value.get, theForm("password").value.get)
        Ok(Json.toJson(User.all))
      }
    })
  }

  def register = Action { implicit request =>
    Ok(views.html.register.render(request.session));
  }
  
  def allUsers = Action { implicit request =>
    Ok(Json.toJson(User.all))
  }
  
  def deleteUser(id: Long) = Action { implicit request =>
    User.delete(id)
     Ok(Json.toJson(User.all))
  }
  
  
  def updateUser(id: Long,key: String,value: String) = Action { implicit request =>
    if(User.existsById(id)){
    	User.findUserById(id).update(key, value)
    	Ok(Json.toJson(User.all))
    }else{
      BadRequest("User not available!")
    }
  }
    
//  def addUser = Action(parse.multipartFormData) { request =>
//    request.body.file("avatar").map { avatar =>
//      import java.io.File
//      val filename = avatar.filename
//      val contentType = avatar.contentType
//      avatar.ref.moveTo(new File(s"/tmp/avatar/$filename"))
//      Ok(views.html.register.render("", request.session));
//    }.getOrElse {
//      Ok(views.html.register.render("", request.session));
//    }
//  }
}