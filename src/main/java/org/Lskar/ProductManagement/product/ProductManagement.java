package org.Lskar.ProductManagement.product;

import org.Lskar.ProductManagement.domain.Product;
import org.Lskar.ProductManagement.utils.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductManagement {

    private final String productId="product_id";

    private final String productName="product_name";

    private  final String productPrice="price";

    private final String productStack="stock";

    private final String allColumns="product_id,product_name,price,stock";

    private final String insertColumns=String.format("%s,%s,%s",productName,productPrice,productStack);


    public int insertProduct(Product product){
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;

        String sql=String.format("insert into products(%s) values(?,?,?)",insertColumns);

        try{
            conn=JDBCUtil.getConnection();
            JDBCUtil.startTransaction(conn);
            if (product.getPrice() < 0) {
                throw new IllegalPriceException("非法商品价格！");
            }
            if (product.getStock() < 0) {
                throw new IllegalStockException("非法商品数量！");
            }
            int count = JDBCUtil.update(conn,ps,sql,product.getName(),product.getPrice(),product.getStock());
            JDBCUtil.commitTransaction(conn);
            return count;

        }
        catch(Exception e){
            e.printStackTrace();
            JDBCUtil.rollbackTransaction(conn);

        }
        finally{
            JDBCUtil.close(conn,ps,rs);
        }
        return 0;
    }

    public Product getProductById(int id){
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;

        String sql=String.format("select %s from products where %s = ?",allColumns,productId);

        try{
            conn=JDBCUtil.getConnection();
            rs=JDBCUtil.select(conn,ps,sql,id);
            if(rs.next()){
                return new Product(rs.getInt(productId),rs.getString(productName),rs.getDouble(productPrice),rs.getInt(productStack));
            }
            else{
                throw new ProductNotFoundException("查询不到产品 "+id);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            JDBCUtil.close(conn,ps,rs);
        }
        return null;
    }

    public ArrayList<Product> getAllProducts(String orderBy){
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;

        String sql=String.format("select %s from products order by ?",allColumns);

        ArrayList<Product> products=new ArrayList<>();
        try{
            conn=JDBCUtil.getConnection();
            rs=JDBCUtil.select(conn,ps,sql,orderBy);
//            while(rs.next()){
//                products.add(new Product(rs.getInt(productId),rs.getString(productName),rs.getDouble(productPrice),rs.getInt(productStack)));
//            }
            if(!rs.next()){
                throw new ProductNotFoundException("查询不到任何产品");
            }
            else {
                do{
                    products.add(new Product(rs.getInt(productId),rs.getString(productName),rs.getDouble(productPrice),rs.getInt(productStack)));
                }while(rs.next());
            }
            return products;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            JDBCUtil.close(conn,ps,rs);
        }
        return null;
    }
    public int updateProduct(Product product,int stock) {
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;

        String sql=String.format("update products set %s = ?, %s = ?, %s = ? where %s = ?",productName,productPrice,productStack,productId);

        try{
            conn=JDBCUtil.getConnection();
            JDBCUtil.startTransaction(conn);
            if(product.getPrice()<0){
                throw new IllegalPriceException("非法价格！");
            }
//            if(stock<0){
//                throw new IllegalStockException("非法购买数量！");
//            }
            if(stock>product.getStock()){
                throw new IllegalStockException("库存不足，无法购买！");
            }
            int count = JDBCUtil.update(conn,ps,sql,product.getName(),product.getPrice(),product.getStock()-stock,product.getId());
            JDBCUtil.commitTransaction(conn);
            return count;
        }
        catch(Exception e){
            e.printStackTrace();
            JDBCUtil.rollbackTransaction(conn);
        }
        finally {
            JDBCUtil.close(conn,ps,rs);
        }
        return 0;
    }


//    public int buyProduct(Product product,int stock) {
//        Connection conn=null;
//        PreparedStatement ps=null;
//        ResultSet rs=null;
//
//        String sql=String.format("update products set %s = ?, %s = ?, %s = ? where %s = ?",productName,productPrice,productStack,productId);
//
//        try{
//            conn=JDBCUtil.getConnection();
//            JDBCUtil.startTransaction(conn);
//            if(product.getPrice()<0){
//                throw new IllegalPriceException("非法价格！");
//            }
////            if(stock<0){
////                throw new IllegalStockException("非法购买数量！");
////            }
//            if(stock>product.getStock()){
//                throw new IllegalStockException("库存不足，无法购买！");
//            }
//            int count = JDBCUtil.update(conn,ps,sql,product.getName(),product.getPrice(),product.getStock()-stock,product.getId());
//            JDBCUtil.commitTransaction(conn);
//            return count;
//        }
//        catch(Exception e){
//            e.printStackTrace();
//            JDBCUtil.rollbackTransaction(conn);
//        }
//        finally {
//            JDBCUtil.close(conn,ps,rs);
//        }
//        return 0;
//    }

    public int deleteProductById(int id) {
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;

        String sql=String.format("delete from products where %s = ?",productId);

        try{
            conn=JDBCUtil.getConnection();
            JDBCUtil.startTransaction(conn);
            int result = JDBCUtil.update(conn,ps,sql,id);
            JDBCUtil.commitTransaction(conn);
            return result;
        }
        catch(Exception e){
            e.printStackTrace();
            JDBCUtil.rollbackTransaction(conn);
        }
        finally {
            JDBCUtil.close(conn,ps,rs);
        }
        return 0;
    }
    

    private List<Product> selectProductByPage(Connection conn,PreparedStatement ps,ResultSet rs,int begin,int limit)throws Exception{

        String sql=String.format("select %s from products limit ?,?",allColumns);
        ArrayList<Product> products=new ArrayList<>();

        conn=JDBCUtil.getConnection();
        rs=JDBCUtil.select(conn,ps,sql,begin,limit);
        if(!rs.next()){
            throw new ProductNotFoundException("分页查询失败！");
        }
        else{
            do {
                products.add(new Product(rs.getInt(productId),rs.getString(productName),rs.getDouble(productPrice),rs.getInt(productStack)));

            }while(rs.next());
        }
        return products;
    }

    private int getProductsCount(Connection conn,PreparedStatement ps,ResultSet rs) throws Exception{

        int count = 0;
        String sql=String.format("select count(%s) count from products",productId);
        conn=JDBCUtil.getConnection();
        rs=JDBCUtil.select(conn,ps,sql,null);

        if(rs.next()){
            count = rs.getInt("count");
        }
        else {
            throw new ProductNotFoundException("查询不到产品记录的数量");

        }
        return count;
    }


    public List<Product> getProductsByPage(int page){

        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        List<Product> products = new ArrayList<>();
        try{
            conn = JDBCUtil.getConnection();
            int limit=3;
            int totalCount=getProductsCount(conn,ps,rs);
            int totalPage=(totalCount%limit!=0) ? totalCount / limit + 1 : totalCount / limit;

            if(page<=0){
                throw new ProductNotFoundException("非法页码！");
            }
            if(page>totalCount){
                throw new ProductNotFoundException("所查询的页码超出最大页数："+totalPage);
            }
            int begin=(page-1)*limit;
            products=selectProductByPage(conn,ps,rs,begin,limit);
            return products;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            JDBCUtil.close(conn,ps,rs);
        }

        return null;
    }




}
