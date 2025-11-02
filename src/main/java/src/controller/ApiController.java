package src.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import src.model.Article;
import src.model.Category;
import src.model.Subcategory;
import src.repository.ArticleRepository;
import src.repository.CategoryRepository;

@Path("/api")
@RequestScoped
public class ApiController {

  @Inject private ArticleRepository articleRepository;
 // @Inject private CategoryRepository categoryRepository;

  @POST
  @Path("/add-article")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createArticle(Article article) {
    try {
      articleRepository.save(article);
      return Response.status(Response.Status.CREATED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Failed to create article: " + e.getMessage())
          .build();
    }
  }
}
