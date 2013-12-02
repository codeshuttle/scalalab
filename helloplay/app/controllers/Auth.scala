package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import models._
import play.api.data.Forms._
import views.html.defaultpages.badRequest

object Auth extends Controller {

  case class Login(email: String, password: String)

  val loginForm = Form(
    mapping(
      "password" -> text,
      "email" -> email)(Login.apply)(Login.unapply) )
      
  private def validateForm(form: Form[Login]) = {
//    Console.println(" form email "+form("email").value.get+" password "+ form("password").value.get)
//    val user : User = User.findUserByEmail(form("email").value.get);
//    Console.println(" User email "+user.email+" password "+user.password)
    if ( !(form("email").value.get isEmpty()) && User.existsByEmail(form("email").value.get) 
        && (User.findUserByEmail(form("email").value.get).password == form("password").value.get )
    ) {
      form
    } else{
      form.withGlobalError("Invalid userid/password")
    }
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold({
      formWithErrors => BadRequest(views.html.login(validateForm(formWithErrors)))
    }, { login =>
      val theForm = validateForm(loginForm.fill(login))
      if (theForm.hasErrors) {
        Ok(views.html.login(theForm))
      } else {
//         + " " + theForm.get.password theForm.get.email 
        //      Redirect(routes.UsersController.index).flashing("notice" -> s"${user.forename} updated!")
        Ok(views.html.index("Ok! User : " + theForm("email").value.get + " you are in.")).withSession((Security.username -> theForm("email").value.get))
      }
    })
  }

  def logout = Action { implicit request =>
    Ok(views.html.login(loginForm)).withNewSession.flashing("success" -> "You are now logged out.")
  }
  
  def login = Action {implicit request =>
    Ok(views.html.login(loginForm))
  }

  def index = Action {
    Redirect(routes.Application.tasks)
  }

}