import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import javax.naming.*;
import java.sql.*;
import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class GenreTable extends HttpServlet
{

  private DataSource dataSource;

  public void init() throws ServletException {
    try {
      Context envCtx = (Context) new InitialContext().lookup("java:comp/env");
      dataSource = (DataSource) envCtx.lookup("jdbc/TestDB");
    } catch (NamingException e) {
      e.printStackTrace();
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) 
  throws ServletException, IOException {

    Connection connection = null;
    PreparedStatement statement = null;

    try {
      connection = dataSource.getConnection();

      String queryMovieId = request.getParameter("movie_id");
      if (queryMovieId != null && !queryMovieId.isEmpty()) {
        statement = connection.prepareStatement(
          "SELECT genres.* FROM genres JOIN (SELECT * FROM genres_in_movies JOIN movies ON genres_in_movies.movie_id=movies.id " +
          "WHERE movies.id = ?) gm ON genres.id=gm.genre_id"
        );
        statement.setInt(1, Integer.valueOf(queryMovieId));
      } else {
        statement = connection.prepareStatement(
          "SELECT * FROM genres"
        );
      }

      // Perform the query
      ResultSet rs = statement.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      ArrayList<Object> rows = new ArrayList<Object>();

      while (rs.next()) {
        Map<String, Object> row = new HashMap<String, Object>();
        for (int iCol = 1; iCol <= rsmd.getColumnCount(); ++iCol) {
          row.put(rsmd.getColumnName(iCol), rs.getObject(iCol));
        }
        rows.add(row);
      }
      rs.close();

      Map<String, Object> result = new HashMap<String, Object>();
      result.put("results", rows.toArray());
      request.setAttribute("result", result);
      request.getRequestDispatcher("/jsonifier").forward(request, response);

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) 
  throws IOException, ServletException {
    doGet(request, response);
  }
}