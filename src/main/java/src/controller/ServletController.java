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
public class ServletController {

  @GET
  public String index() {
    return "list.xhtml";
  }

  @GET
  @Path("/detail")
  public String detail() {
    return "detail.xhtml";
  }
}
