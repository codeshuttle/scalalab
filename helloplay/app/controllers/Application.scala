package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import models._
import play.api.data.Forms._
import views.html.defaultpages.badRequest

object Application extends Controller with Secured{

  val taskForm = Form(
    "label" -> nonEmptyText)

  def index = Action {
    Redirect(routes.Application.tasks)
  }

  def tasks = withUser {user => implicit request =>
    Ok(views.html.task(Task.all(), taskForm))
  }

  def newTask = withUser { user => implicit request =>
    taskForm.bindFromRequest.fold(
      errors => BadRequest(views.html.task(Task.all(), errors)),
      label => {
        //      Console.println ( " newTask : label = " + label ) 
        Task.create(label)
        Redirect(routes.Application.tasks)
      })
  }

  def deleteTask(id: Long) = withUser {user => implicit request =>
    //    Console.println ( " deleteTask : id = " + id ) 
    Task.delete(id)
    Redirect(routes.Application.tasks)
  }

}