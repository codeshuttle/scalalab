package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import crypto._

case class User(id:Long,var name:String,var email:String,var password:String){
  
  def update(key:String,value:String) = {
     key match {
      case "name"=> this.name = value
      case "email"=> this.email = value
      case "password"=> this.password = value
    }
    User.update(id,name,email,password)
  }
}

object User{
  
  val user = {
    get[Long]("id") ~
    get[String]("name") ~
    get[String]("email") ~
      get[String]("password") map {
        case id ~ name ~ email ~ password => User(id, name, email, Encrypt.decrypt(password)  ) 
      }
  }
  
  def exists(name:String): Boolean = DB.withConnection { implicit c =>
     ! (SQL("select * from user where name = {name}").on(
        'name -> name).as(user *) isEmpty)
  }
  
  def existsByEmail(email:String): Boolean = DB.withConnection { implicit c =>
     ! (SQL("select * from user where email = {email}").on(
        'email -> email).as(user *) isEmpty)
  }
  
  def findUserByEmail(email:String): User = DB.withConnection { implicit c =>
    SQL("select * from user where email = {email}").on(
        'email -> email).as(user *) head
  }
  
  def existsById(id:Long): Boolean = DB.withConnection { implicit c =>
     ! (SQL("select * from user where id = {id}").on(
        'id -> id).as(user *) isEmpty)
  }
  
  def findUserById(id:Long): User = DB.withConnection { implicit c =>
    SQL("select * from user where id = {id}").on(
        'id -> id).as(user *) head
  }
  
  def findUser(name:String): User = DB.withConnection { implicit c =>
    SQL("select * from user where name = {name}").on(
        'name -> name).as(user *) head
  }
  
  def all(): List[User] = DB.withConnection { implicit c =>
    SQL("select * from user").as(user *)
  }
  
  def update(id:Long,name:String,email:String,password:String)  {
    DB.withConnection { implicit c =>
      SQL("update user set name={name},email={email},password={password} where id={id}").on(
        'id -> id,
        'name -> name,
        'email -> email,
        'password -> Encrypt.encrypt(password)
        ).executeUpdate()
    }
  }
  
  
  def create(name:String,email:String,password:String)  {
    DB.withConnection { implicit c =>
      SQL("insert into user (name,email,password) values ({name},{email},{password})").on(
        'name -> name,
        'email -> email,
        'password -> Encrypt.encrypt(password)
        ).executeUpdate()
    }
  }
  
  def delete(id: Long)  {
    DB.withConnection { implicit c =>
      SQL("delete from user where id = {id}").on(
        'id -> id).executeUpdate()
    }
  }
}