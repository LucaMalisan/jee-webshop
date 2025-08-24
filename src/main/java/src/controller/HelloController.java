package src.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.mvc.Controller;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.extern.java.Log;

@Controller
@RequestScoped
@Log
@Path("/")
public class HelloController {

  @GET
  public String index() {
    return "list.xhtml";
  }
}
