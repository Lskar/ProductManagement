package org.Lskar.ProductManagement.product;

import org.Lskar.ProductManagement.domain.Product;
import org.Lskar.ProductManagement.utils.IllegalPriceException;
import org.Lskar.ProductManagement.utils.IllegalStockException;
import org.Lskar.ProductManagement.utils.JDBCUtil;
import org.Lskar.ProductManagement.utils.ProductNotFoundException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductManagement {

    private static final String PRODUCT_ID = "product_id";

    private static final String PRODUCT_NAME = "product_name";

    private static final String PRODUCT_PRICE = "price";

    private static final String PRODUCT_STOCK = "stock";

//    private static final String allColumns = "product_id,product_name,price,stock";
//
//    private static final String insertColumns = String.format("%s,%s,%s", productName, productPrice, productStack);

    Connection conn;

    public ProductManagement() throws Exception {
        conn = JDBCUtil.getConnection();
    }

    public void closeConnection(){
        try{
            if(conn != null){
                conn.close();
            }
        } catch(Exception e){
            e.printStackTrace(System.err);
        }
    }


    public int insertProduct(Product product) {


        //String sql = String.format("insert into products(%s) values(?,?,?)", insertColumns);
        String sql = "insert into products(product_name,price, stock) values(?,?,?)";
        try {
            JDBCUtil.startTransaction(conn);
            if (product.getPrice() < 0) {
                throw new IllegalPriceException("非法商品价格："+product.getPrice());
            }
            if (product.getStock() < 0) {
                throw new IllegalStockException("非法商品数量："+product.getStock());
            }
            int count = JDBCUtil.update(conn, sql, product.getName(), product.getPrice(), product.getStock());
            JDBCUtil.commitTransaction(conn);
            return count;

        } catch (Exception e) {
            JDBCUtil.rollbackTransaction(conn);
            throw e;

        }
    }

    public Product getProductById(int id) throws Exception{


        ResultSet rs = null;

        //String sql = String.format("select %s from products where %s = ?", allColumns, productId);
        String sql ="select product_id,product_name,price,stock from products where product_id = ?";
        try {
            rs = JDBCUtil.select(conn, sql, id);
            if (rs.next()) {
                return new Product(rs.getInt(PRODUCT_ID), rs.getString(PRODUCT_NAME), rs.getDouble(PRODUCT_PRICE),
                        rs.getInt(PRODUCT_STOCK));
            } else {
                throw new ProductNotFoundException("查询不到产品 " + id);
            }
        } finally {
            JDBCUtil.close(null, null, rs);
        }
    }

    public ArrayList<Product> getAllProducts(String orderBy) throws Exception{

        ResultSet rs = null;

        //String sql = String.format("select %s from products order by %s", allColumns,orderBy);
        String sql = String.format("select product_id,product_name,price,stock from products order by %s",orderBy);
        ArrayList<Product> products = new ArrayList<>();
        try {

            rs=JDBCUtil.select(conn,sql);
            if (!rs.next()) {
                throw new ProductNotFoundException("查询不到任何产品");
            } else {
                do {
                    products.add(
                            new Product(rs.getInt(PRODUCT_ID), rs.getString(PRODUCT_NAME), rs.getDouble(PRODUCT_PRICE),
                                    rs.getInt(PRODUCT_STOCK)));
                } while (rs.next());
            }
            return products;
        } finally {
            JDBCUtil.close(null, null, rs);
        }
    }

    public int updateProduct(Product product, int stock) {



        //String sql = String.format("update products set %s = ?, %s = ?, %s = ? where %s = ?", productName, productPrice,
         //       productStack, productId);
        String sql = "update products set product_name = ?, price = ?, stock = ? where product_id = ?";

        try {

            JDBCUtil.startTransaction(conn);
            if (product.getPrice() < 0) {
                throw new IllegalPriceException("非法价格！");
            }
            if (stock > product.getStock()) {
                throw new IllegalStockException("库存不足，无法购买！");
            }
            int count = JDBCUtil.update(conn, sql, product.getName(), product.getPrice(), product.getStock() - stock,
                    product.getId());
            JDBCUtil.commitTransaction(conn);
            return count;
        } catch (Exception e) {
            JDBCUtil.rollbackTransaction(conn);
            throw e;
        }
    }


    public int deleteProductById(int id) {

//        String sql = String.format("delete from products where %s = ?", productId);
        String sql = "delete from products where product_id = ?";

        try {
            JDBCUtil.startTransaction(conn);
            int result = JDBCUtil.update(conn, sql, id);
            JDBCUtil.commitTransaction(conn);
            return result;
        } catch (Exception e) {
            JDBCUtil.rollbackTransaction(conn);
            throw e;
        }
    }


    private List<Product> selectProductByPage(int begin, int limit) throws Exception {

        ResultSet rs = null;
//        String sql = String.format("select %s from products limit ?,?", allColumns);
        String sql = "select product_id,product_name,price,stock from products limit ?,?";
        ArrayList<Product> products = new ArrayList<>();

        try {
            rs = JDBCUtil.select(conn, sql, begin, limit);
            if (!rs.next()) {
                throw new ProductNotFoundException("分页查询失败！");
            } else {
                do {
                    products.add(new Product(rs.getInt(PRODUCT_ID), rs.getString(PRODUCT_NAME), rs.getDouble(PRODUCT_PRICE),
                            rs.getInt(PRODUCT_STOCK)));

                } while (rs.next());
            }
            return products;
        }finally {
            JDBCUtil.close(null, null, rs);
        }
    }

    private int getProductsCount () throws Exception {

        int count;
        ResultSet rs =null;
//        String sql = String.format("select count(%s) count from products", productId);
        String sql = "select count(product_id) count from products";
        try {
            rs = JDBCUtil.select(conn, sql);

            if (rs.next()) {
                count = rs.getInt("count");
            } else {
                throw new ProductNotFoundException("查询不到产品记录的数量");

            }
            return count;
        } finally {
            JDBCUtil.close(null, null, rs);
        }
    }

    public List<Product> getProductsByPage(int page) throws Exception {

        List<Product> products;
        int limit = 3;
        int totalCount = getProductsCount();
        int totalPage = (totalCount % limit != 0) ? totalCount / limit + 1 : totalCount / limit;
        if (page <= 0) {
            throw new ProductNotFoundException("非法页码！");
        }
        if (page > totalPage) {
            throw new ProductNotFoundException("所查询的页码超出最大页数：" + totalPage);
        }
        int begin = (page - 1) * limit;
        products = selectProductByPage(begin,limit);
        return products;

    }

}
