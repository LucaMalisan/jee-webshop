package src.bean;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.java.Log;

import java.io.Serializable;
import src.model.HelloWorld;

@Named("helloBean")
@Log
@ViewScoped
public class HelloBean implements Serializable {

    @PersistenceContext
    EntityManager entitymanager;

    public String getMessage() {
        return entitymanager.find(HelloWorld.class, 1).getHello();
    }
}
