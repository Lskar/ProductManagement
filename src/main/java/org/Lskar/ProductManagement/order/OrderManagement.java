package org.Lskar.ProductManagement.order;

import org.Lskar.ProductManagement.domain.Item;
import org.Lskar.ProductManagement.domain.Order;
import org.Lskar.ProductManagement.domain.Product;
import org.Lskar.ProductManagement.product.ProductManagement;
import org.Lskar.ProductManagement.utils.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OrderManagement {


    private static final String ORDER_ID="order_id";

    private static final String ORDER_TIME="order_time";

    private static final String TOTAL_PRICE="total_price";

    private static final String PRODUCT_ID="product_id";

    private static final String QUANTITY="quantity";

//    private final String orderItemsAllColumn=String.format("%s,%s,%s",orderId,productId,quantity);
//
//    private final String ordersAllColumn=String.format("%s,%s,%s",orderId,orderTime,totalPrice);
    private final ProductManagement productManagement=new ProductManagement();

    Connection conn;

    public OrderManagement() throws Exception{
        conn=JDBCUtil.getConnection();
    }

    public void closeConnection(){
        try{
            if(conn!=null){
                conn.close();
            }
        } catch(Exception e){
            e.printStackTrace(System.err);
        }
    }


    public int insertOrder(Order order) throws Exception{

        ResultSet rs = null;
//        String sql=String.format("insert into orders (%s) values(?)",totalPrice);
        String sql="insert into orders (total_price) values(?)";
        try{

            JDBCUtil.startTransaction(conn);
            if(order.getTotalPrice()<0){
                throw new IllegalPriceException("非法的订单总价！");
            }
            rs=JDBCUtil.getPreviousResult(conn,sql,order.getTotalPrice());
            if(rs.next()){
                order.setOrderID(rs.getInt(1));
            }
            insertOrderItems(order);
            JDBCUtil.commitTransaction(conn);
            return order.getOrderID();
        }
        catch(Exception e){
            JDBCUtil.rollbackTransaction(conn);
            throw e;
        }
        finally {
            JDBCUtil.close(null,null,rs);

        }
    }

    private void insertOrderItems(Order order)throws Exception{

//        String sql=String.format("insert into order_items (%s) values(?,?,?)",orderItemsAllColumn);
        String sql="insert into order_items (order_id,product_id,quantity) values(?,?,?)";
        List<Item> items=order.getItems();
        for(Item item:items){
            Product product =productManagement.getProductById(item.getId());
            if(product==null){
                throw new ProductNotFoundException("查询产品出错！");
            }
            int result=productManagement.updateProduct(product,item.getQuantity());
            if(result==0){
                throw new ErrorInUpdateException("无法更新产品 "+product.getId()+" 的库存");
            }
            JDBCUtil.update(conn,sql,order.getOrderID(),item.getId(),item.getQuantity());
        }

    }

    public int deleteOrderById(int id) {

//        String sql=String.format("delete from orders where %s = ?",orderId);
        String sql="delete from orders where order_id = ?";

        try{

            JDBCUtil.startTransaction(conn);
            int result=JDBCUtil.update(conn,sql,id);
            JDBCUtil.commitTransaction(conn);
            return result;
        }
        catch(Exception e){
            JDBCUtil.rollbackTransaction(conn);
            throw e;
        }
    }

    public Order getOrderById(int id) throws Exception{


        ResultSet rs=null;
//        String sql=String.format("select %s from orders where %s = ?",ordersAllColumn,orderId);

        String sql="select order_id,order_time,total_price from orders where order_id = ?";
        Order order;
        try{

            rs=JDBCUtil.select(conn,sql,id);
            if(rs.next()){
                order=new Order(rs.getInt(ORDER_ID),rs.getTimestamp(ORDER_TIME),rs.getDouble(TOTAL_PRICE));
            }
            else{
                throw new OrderNotFoundException("查询不到订单 "+id);
            }
            order.setItems(getItemsByOrderId(id));

            return order;

        } finally {
            JDBCUtil.close(null,null,rs);
        }

    }

    private List<Item> getItemsByOrderId(int id) throws Exception{
        ResultSet rs = null;
//        String sql=String.format("select %s from order_items where order_id = ?",orderItemsAllColumn);
        String sql="select order_id,product_id,quantity from order_items where order_id = ?";
        List<Item> items= new ArrayList<>();
        try {
            rs=JDBCUtil.select(conn,sql,id);
            while(rs.next()){
                items.add(new Item(rs.getInt(PRODUCT_ID), rs.getInt(QUANTITY)));
            }
            return items;
        } finally {
            JDBCUtil.close(null,null,rs);
        }
    }

    public List<Order> getAllOrdersOrderBy(String orderBy) throws Exception{

        ResultSet rs=null;

//        String sql=String.format("select %s from orders order by %s",ordersAllColumn,orderBy);
        String sql=String.format("select order_id,order_time,total_price from orders order by %s",orderBy);
        List<Order> orders= new ArrayList<>();
        try{
            rs=JDBCUtil.select(conn,sql);
            if(!rs.next()){
                throw new OrderNotFoundException("查询不到任何订单");
            }
            else{
                do{
                    orders.add(getOrderById(rs.getInt(ORDER_ID)));
                }while(rs.next());
            }
            return orders;
        } finally {
            JDBCUtil.close(null,null,rs);
        }

    }

    private int getOrdersCount() throws Exception{

        int count;
        ResultSet rs = null;
        //String sql=String.format("select count(%s) count from orders",orderId);
        String sql="select count(order_id) count from orders";
        try {
            rs=JDBCUtil.select(conn,sql);
            if(rs.next()){
                count = rs.getInt("count");
            }
            else{
                throw new OrderNotFoundException("查询不到订单记录的数量");
            }
            return count;
        } finally {
            JDBCUtil.close(null,null,rs);
        }
    }

    private List<Order> selectOrderByPage(int begin,int limit) throws Exception{
        ResultSet rs = null;
        //String sql=String.format("select %s from orders limit ?,?",ordersAllColumn);
        String sql="select order_id,order_time,total_price from orders limit ?,?";
        List<Order> orders=new ArrayList<>();
        try {
            rs=JDBCUtil.select(conn,sql,begin,limit);
            if(!rs.next()){
                throw new OrderNotFoundException("分页查询失败");
            }
            else{
                do{
                    orders.add(getOrderById(rs.getInt(ORDER_ID)));
                }
                while(rs.next());
            }
            return orders;
        } finally {
            JDBCUtil.close(null,null,rs);
        }
    }

    public List<Order> getOrdersByPage(int page) throws Exception{

        List<Order> orders;
        int limit=2;
        int totalCount=getOrdersCount();
        int totalPage=(totalCount%limit!=0) ? totalCount / limit + 1 : totalCount / limit;
        if(page<=0){
            throw new ProductNotFoundException("非法页码！");
        }
        if(page>totalPage){
            throw new ProductNotFoundException("所查询的页码超出最大页数："+totalPage);
        }
        int begin=(page-1)*limit;
        orders=selectOrderByPage(begin,limit);
        return orders;
    }

    private Item getItemById(int id,int itemId) throws Exception{
        ResultSet rs = null;
        //String sql=String.format("select %s from order_items where %s = ? and %s = ?",orderItemsAllColumn,orderId,productId);
        String sql="select order_id,product_id,quantity from order_items where order_id = ? and product_id = ?";
        try {
            rs=JDBCUtil.select(conn,sql,id,itemId);
            if(rs.next()){
                return new Item(rs.getInt(PRODUCT_ID),rs.getInt(QUANTITY));
            }
            else{
                throw new OrderNotFoundException("找不到此条记录:"+id+"号订单，"+itemId+"号商品");
            }
        } finally {
            JDBCUtil.close(null,null,rs);
        }
    }
    private void updateItem(Item oldItem,int id,int number) {
        //String sql=String.format("update order_items set %s = ? where %s = ? and %s = ?",quantity,orderId,productId);
        String sql="update order_items set quantity = ? where order_id = ? and product_id = ?";
        JDBCUtil.update(conn,sql,oldItem.getQuantity()+number,id,oldItem.getId());
    }

    private void updateOrderPriceById(int id,int itemId,int number) throws Exception{
//        String sql=String.format("update orders set %s = ? where %s = ?",totalPrice,orderId);
        String sql="update orders set total_price = ? where order_id = ?";
        Order order = getOrderById(id);
        Product product = productManagement.getProductById(itemId);
        JDBCUtil.update(conn,sql,order.getTotalPrice()+number*product.getPrice(),id);
    }

    private void deleteItem(int id,int itemId) {
//        String sql=String.format("delete from order_items where %s = ? and %s = ?",orderId,productId);
        String sql="delete from order_items where order_id = ? and product_id = ?";
        JDBCUtil.update(conn,sql,id,itemId);
    }




    public int updateOrderById(int id,int itemId,int number)throws Exception{

        ProductManagement productManagement=new ProductManagement();

        try{
            JDBCUtil.startTransaction(conn);
            Item oldItem= getItemById(id,itemId);

            //修改order_items表
            if(oldItem.getQuantity()+number<0){
                throw new IllegalStockException("无法修改购买数量，非法的数据");
            }
            else if(oldItem.getQuantity()+number==0){
                deleteItem(id,itemId);
            }
            else{
                updateItem(oldItem,id,number);
            }

            //修改products表
            int result=productManagement.updateProduct(productManagement.getProductById(itemId),number);
            if(result==0){
                throw new IllegalStockException("无法修改库存数量");
            }

            //修改orders表
            updateOrderPriceById(id,itemId,number);

            JDBCUtil.commitTransaction(conn);

            return result;
        }
        catch(Exception e){
            JDBCUtil.rollbackTransaction(conn);
            throw e;
        }
    }
}
