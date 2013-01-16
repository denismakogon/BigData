package com.mirantis.aminakov.bigdatacourse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class DaoJdbc implements Dao {
	String driverName = "com.mysql.jdbc.Driver";
	String url = "jdbc:mysql://localhost:1234/bigdata?user=aminakov&password=bigdata";
	String jdbcutf8 = "&useUnicode=true&characterEncoding=UTF-8";
	Connection con = null;
	Statement st = null;
	ResultSet rs = null;
	PreparedStatement pst = null;
	
	public DaoJdbc() throws InstantiationException, IllegalAccessException, DaoException {
		try {
			Class.forName(driverName).newInstance();
			try {
				con = DriverManager.getConnection(url + jdbcutf8);
			} catch (SQLException e) {
				throw new DaoException(e);
			}
		} catch (ClassNotFoundException e) {
			System.err.println("Driver not found.");
			System.exit(0);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.mirantis.aminakov.bigdatacourse.Dao#addBook(com.mirantis.aminakov.bigdatacourse.Book)
	 */
	@Override
	public int addBook(Book book) throws DaoException {
		int count = 0;
		try {
			con.setAutoCommit(false);
			st = con.createStatement();
			int book_id = 0;
			String sql = "INSERT INTO Texts(text) VALUES (?)";
			pst = con.prepareStatement(sql);
			pst.setBlob(1, book.getText());
			pst.executeUpdate();
			rs = st.executeQuery("SELECT LAST_INSERT_ID();");
			while (rs.next()) {
				book_id = rs.getInt("LAST_INSERT_ID()");
				System.out.println(book_id);
			}
			pst.clearParameters();
			pst = null;
			pst = con.prepareStatement("INSERT INTO Books (title, book_id, author_id, genre_id) VALUES (?, ?, ?, ?);");
			pst.setString(1, book.getTitle());
			pst.setInt(2, book_id);
			rs = st.executeQuery("SELECT * FROM Authors WHERE author = '" + book.getAuthor() + "';");
			if (rs.first()) {
				pst.setString(3, rs.getString("id"));
			} else {
				int author_id = 0;
				st.executeUpdate("INSERT INTO Authors(author) VALUES ('" + book.getAuthor() + "');");
				rs = st.executeQuery("SELECT LAST_INSERT_ID();");
				while (rs.next()) {
					author_id = rs.getInt("LAST_INSERT_ID()");
					System.out.println(author_id);
				}
				pst.setInt(3, author_id);
			}
			rs = st.executeQuery("SELECT * FROM Genres WHERE genre = '" + book.getGenre() + "';");
			if (rs.first()) {
				pst.setString(4, rs.getString("id"));
			} else {
				int genre_id = 0;
				st.executeUpdate("INSERT INTO Genres(genre) VALUES ('" + book.getGenre() + "');");
				rs = st.executeQuery("SELECT LAST_INSERT_ID();");
				while (rs.next()) {
					genre_id = rs.getInt("LAST_INSERT_ID()");
					System.out.println(genre_id);
				}
				pst.setInt(4, genre_id);
			}
			pst.executeUpdate();
			pst.clearParameters();
			con.commit();
			con.setAutoCommit(true);
		} catch (SQLException e) {
			try {
				con.rollback();
				throw new DaoException("Transaction filed " + e.getMessage());
			} catch (SQLException e1) {
				throw new DaoException("Rollback filed " + e1.getMessage());
			}
		} finally {
            if (rs != null) {
                try {
					rs.close();
				} catch (SQLException e) {
					throw new DaoException(e);
				}
            }
            if (st != null) {
                try {
					st.close();
				} catch (SQLException e) {
					throw new DaoException(e);
				}
            }
            if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					throw new DaoException(e);
				}
			}
        }
		return count;
	}
	
	/* (non-Javadoc)
	 * @see com.mirantis.aminakov.bigdatacourse.Dao#getAllBooks(int, int)
	 */
	@Override
	public List<Book> getAllBooks(int pageNum, int pageSize) throws DaoException {
		List<Book> books = new ArrayList<Book>();
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT * FROM Books JOIN Authors ON Books.author_id=Authors.id " + 
					"JOIN Genres ON Books.genre_id=Genres.id " +
					"JOIN Texts ON Books.book_id=Texts.id LIMIT " + (pageNum-1) * pageSize  + "," + pageSize);
			BookMapper map = new BookMapper();
			while (rs.next()) {
				books.add((Book)map.mapRow(rs, 0));	
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
            if (rs != null) {
                try {
					rs.close();
				} catch (SQLException e) {
					throw new DaoException(e);
				}
            }
            if (st != null) {
                try {
					st.close();
				} catch (SQLException e) {
					throw new DaoException(e);
				}
            }
        }
		return books;
	}
	
	/* (non-Javadoc)
	 * @see com.mirantis.aminakov.bigdatacourse.Dao#getBookByTitle(int, int, java.lang.String)
	 */
	@Override
	public List<Book> getBookByTitle(int pageNum, int pageSize, String title) throws DaoException {
		List<Book> books = new ArrayList<Book>();
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT * FROM Books JOIN Authors ON Books.author_id=Authors.id " + 
					"JOIN Genres ON Books.genre_id=Genres.id JOIN Texts ON Books.book_id=Texts.id " + 
					"WHERE title = '" + title + "' LIMIT " + (pageNum-1) * pageSize  + "," + pageSize);
			BookMapper map = new BookMapper();
			while (rs.next()) {
				books.add((Book)map.mapRow(rs, 0));			
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
            if (rs != null) {
                try {
					rs.close();
				} catch (SQLException e) {
					throw new DaoException(e);
				}
            }
            if (st != null) {
                try {
					st.close();
				} catch (SQLException e) {
					throw new DaoException(e);
				}
            }
        }
		return books;
	}
	
	/* (non-Javadoc)
	 * @see com.mirantis.aminakov.bigdatacourse.Dao#getBookByAuthor(int, int, java.lang.String)
	 */
	@Override
	public List<Book> getBookByAuthor(int pageNum, int pageSize, String author) throws DaoException {
		List<Book> books = new ArrayList<Book>();
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT * FROM Books JOIN Authors ON Books.author_id=Authors.id " + 
					"JOIN Genres ON Books.genre_id=Genres.id JOIN Texts ON Books.book_id=Texts.id " + 
					"WHERE author = '" + author + "' LIMIT " + (pageNum-1) * pageSize  + "," + pageSize);
			BookMapper map = new BookMapper();
			while (rs.next()) {
				books.add((Book)map.mapRow(rs, 0));			
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
            if (rs != null) {
                try {
					rs.close();
				} catch (SQLException e) {
					throw new DaoException(e);
				}
            }
            if (st != null) {
                try {
					st.close();
				} catch (SQLException e) {
					throw new DaoException(e);
				}
            }
        }
		return books;
	}
	
	/* (non-Javadoc)
	 * @see com.mirantis.aminakov.bigdatacourse.Dao#getBookByGenre(int, int, java.lang.String)
	 */
	@Override
	public List<Book> getBookByGenre(int pageNum, int pageSize, String genre) throws DaoException {
		List<Book> books = new ArrayList<Book>();
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT * FROM Books JOIN Authors ON Books.author_id=Authors.id " + 
					"JOIN Genres ON Books.genre_id=Genres.id JOIN Texts ON Books.book_id=Texts.id " + 
					"WHERE genre = '" + genre + "' LIMIT " + (pageNum-1) * pageSize  + "," + pageSize);
			BookMapper map = new BookMapper();
			while (rs.next()) {
				books.add((Book)map.mapRow(rs, 0));			
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
            if (rs != null) {
                try {
					rs.close();
				} catch (SQLException e) {
					throw new DaoException(e);
				}
            }
            if (st != null) {
                try {
					st.close();
				} catch (SQLException e) {
					throw new DaoException(e);
				}
            }
        }
		return books;
	}	
	
	/* (non-Javadoc)
	 * @see com.mirantis.aminakov.bigdatacourse.Dao#getAuthorByGenre(int, int, java.lang.String)
	 */
	@Override
	public TreeSet<String> getAuthorByGenre(int pageNum, int pageSize, String genre) throws DaoException {
		TreeSet<String> authors = new TreeSet<String>();
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT author FROM Books JOIN Authors ON Books.author_id=Authors.id " + 
					"JOIN Genres ON Books.genre_id=Genres.id " + 
					"WHERE genre = '" + genre + "' LIMIT " + (pageNum-1) * pageSize  + "," + pageSize);
			while (rs.next()) {
				String name = new String();
				name = rs.getString("author");
				authors.add(name);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
            if (rs != null) {
                try {
					rs.close();
				} catch (SQLException e) {
					throw new DaoException(e);
				}
            }
            if (st != null) {
                try {
					st.close();
				} catch (SQLException e) {
					throw new DaoException(e);
				}
            }
        }
		return authors;
	}

}