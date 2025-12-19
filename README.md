# jee-webshop
<h2> Inhalte des Repositories </h2>

<ul>
  <li> ARC42-Dokumentation: https://github.com/LucaMalisan/jee-webshop/blob/main/documentation.pdf </li>
  <li> Swagger Dokumentation: https://lucamalisan.github.io/jee-webshop/index.html </li>
  <li> Fokusthema Präsentation: https://github.com/LucaMalisan/jee-webshop/blob/main/JEE%20Pr%C3%A4sentation.pdf</li>
  <li> Dockerfile: https://github.com/LucaMalisan/jee-webshop/blob/main/Dockerfile</li>
</ul>

<h2> Installation </h2>
Die Datenbank läuft extern in der Google Cloud, weshalb dazu keine weitere Installation nötig ist. 
Um die Applikation zu deployen, muss das Dockerfile ausgeführt werden. Wichtig ist, dass die Applikation auf Port 8080 läuft, dass die Authentifizierung mit Auth0 korrekt funktioniert. 
Die Applikation ist dann <a href="http://localhost:8080/jee-webshop/application"> hier </a> erreichbar

<h2> Unit Tests </h2>
Bei jedem Unit Test werden automatisch Jacoco Reports generiert. Um dieses Verhalten zu triggern, kann lokal folgender Command ausgeführt werden: <br>
<i> mvn install </i> <br>
Die generierten Reports liegen unter target/site/jacoco
