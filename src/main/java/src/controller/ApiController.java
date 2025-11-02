package src.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import src.model.Article;
import src.model.ArticleImage;
import src.model.Category;
import src.model.Subcategory;
import src.repository.ArticleRepository;
import src.repository.CategoryRepository;

@Path("/api")
@RequestScoped
public class ApiController {

  @Inject private ArticleRepository articleRepository;
  @Inject private CategoryRepository categoryRepository;

  // Article endpoints
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

  @POST
  @Path("/update-article")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateArticle(Article article) {
    try {
      articleRepository.merge(article);
      return Response.status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Failed to update article: " + e.getMessage())
          .build();
    }
  }

  @DELETE
  @Path("/delete-article/{sku}")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteArticle(@PathParam("sku") String sku) {
    try {
      articleRepository.deleteBySku(sku);
      return Response.status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Failed to delete article: " + e.getMessage())
          .build();
    }
  }

  // Category endpoints
  @POST
  @Path("/add-category")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createCategory(Category category) {
    try {
      categoryRepository.save(category);
      return Response.status(Response.Status.CREATED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Failed to create category: " + e.getMessage())
          .build();
    }
  }

  @POST
  @Path("/update-category")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateCategory(Category category) {
    try {
      categoryRepository.merge(category);
      return Response.status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Failed to update category: " + e.getMessage())
          .build();
    }
  }

  @DELETE
  @Path("/delete-category/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteCategory(@PathParam("uuid") String uuid) {
    try {
      categoryRepository.deleteByUuid(uuid);
      return Response.status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Failed to delete category: " + e.getMessage())
          .build();
    }
  }

  // Subcategory endpoints
  @POST
  @Path("/add-subcategory")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createSubcategory(Subcategory subcategory) {
    try {
      categoryRepository.save(subcategory);
      return Response.status(Response.Status.CREATED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Failed to create subcategory: " + e.getMessage())
          .build();
    }
  }

  @POST
  @Path("/update-subcategory")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateSubcategory(Subcategory subcategory) {
    try {
      categoryRepository.merge(subcategory);
      return Response.status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Failed to update subcategory: " + e.getMessage())
          .build();
    }
  }

  @DELETE
  @Path("/delete-subcategory/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteSubcategory(@PathParam("uuid") String uuid) {
    try {
      categoryRepository.deleteBySubcategoryUuid(uuid);
      return Response.status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Failed to delete subcategory: " + e.getMessage())
          .build();
    }
  }

  // ArticleImage endpoints
  @POST
  @Path("/add-article-image")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createArticleImage(ArticleImage articleImage) {
    try {
      articleRepository.save(articleImage);
      return Response.status(Response.Status.CREATED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Failed to create article image: " + e.getMessage())
          .build();
    }
  }

  @POST
  @Path("/update-article-image")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateArticleImage(ArticleImage articleImage) {
    try {
      articleRepository.merge(articleImage);
      return Response.status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Failed to update article image: " + e.getMessage())
          .build();
    }
  }

  @DELETE
  @Path("/delete-article-image/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteArticleImage(@PathParam("uuid") String uuid) {
    try {
      articleRepository.deleteByArticleImageUuid(uuid);
      return Response.status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Failed to delete article image: " + e.getMessage())
          .build();
    }
  }
}
