package org.cogcomp.taxorel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuany on 12/1/2017.
 */
public class CategoryCache implements Runnable{
    String _file = "";
    public CategoryCache (String input){
        _file = input;
    }
    public void run(){
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/wiki", "root", "PASSWORD");
            int count = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(_file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    count ++;
                    System.out.println(_file + ": " + count);
                    String cat = line;
                    List<String> superCats = WikiHandler.getParentCategory(cat, conn);
                    String super_cat_concat = "";
                    for (String s : superCats){
                        super_cat_concat += s + "||";
                    }
                    String updateQuery = "UPDATE category SET super_cats=? WHERE cat_title=?";
                    PreparedStatement preparedStatement = conn.prepareStatement(updateQuery);
                    preparedStatement.setString(1, super_cat_concat);
                    preparedStatement.setString(2, cat);
                    preparedStatement.execute();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void exp(){
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/wiki", "root", "PASSWORD");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT CONVERT(category.cat_title USING utf8) FROM category");
            double progress = 0.0;
            double total = 1656102.0;
            while (rs.next()){
                progress += 1.0;
                String cat = (String)rs.getObject(1);
                List<String> superCats = WikiHandler.getParentCategory(cat, conn);
                String super_cat_concat = "";
                for (String s : superCats){
                    super_cat_concat += s + "||";
                }
                String updateQuery = "UPDATE category SET super_cats=? WHERE cat_title=?";
                PreparedStatement preparedStatement = conn.prepareStatement(updateQuery);
                preparedStatement.setString(1, super_cat_concat);
                preparedStatement.setString(2, cat);
                preparedStatement.execute();
                System.out.println("Current progress: " + new DecimalFormat("##.##").format(progress / total * 100.0) + "%");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void exportCats(){
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/wiki", "root", "PASSWORD");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT CONVERT(category.cat_title USING utf8) FROM category");
            ArrayList<String> lines = new ArrayList<>();
            while (rs.next()){
                String cat = (String)rs.getObject(1);
                lines.add(cat + "\n");
            }
            DataHandler.writeLines(lines, "data/categories.list");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        exportCats();
    }
}
