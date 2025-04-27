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


    private final String orderId="order_id";

    private final String orderTime="order_time";

    private final String totalPrice="total_price";

    private final String productId="product_id";

    private final String quantity="quantity";

    private final String orderItemsAllColumn=String.format("%s,%s,%s",orderId,productId,quantity);

    private final String ordersAllColumn=String.format("%s,%s,%s",orderId,orderTime,totalPrice);

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
            e.printStackTrace();
        }
    }


    public int insertOrder(Order order) throws Exception{

        ResultSet rs = null;
        String sql=String.format("insert into orders (%s) values(?)",totalPrice);
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

        String sql=String.format("insert into order_items (%s) values(?,?,?)",orderItemsAllColumn);
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

    public int deleteOrderById(int id) throws Exception{

        String sql=String.format("delete from orders where %s = ?",orderId);
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
        String sql=String.format("select %s from orders where %s = ?",ordersAllColumn,orderId);
        Order order=null;
        try{

            rs=JDBCUtil.select(conn,sql,id);
            if(rs.next()){
                order=new Order(rs.getInt(orderId),rs.getTimestamp(orderTime),rs.getDouble(totalPrice));
            }
            else{
                throw new OrderNotFoundException("查询不到订单 "+id);
            }
            order.setItems(getItemsByOrderId(conn,rs,id));

            return order;

        } finally {
            JDBCUtil.close(null,null,rs);
        }

    }

    private List<Item> getItemsByOrderId(Connection conn,ResultSet rs,int id) throws Exception{

        String sql=String.format("select %s from order_items where order_id = ?",orderItemsAllColumn);
        List<Item> items=new ArrayList<Item>();
        rs=JDBCUtil.select(conn,sql,id);
        while(rs.next()){
            items.add(new Item(rs.getInt(productId), rs.getInt(quantity)));
        }
        return items;
    }

    public List<Order> getAllOrdersOrderBy(String orderBy) throws Exception{

        ResultSet rs=null;

        String sql=String.format("select %s from orders order by %s",ordersAllColumn,orderBy);
        List<Order> orders= new ArrayList<>();
        try{
            conn=JDBCUtil.getConnection();
            rs=JDBCUtil.select(conn,sql);
            if(!rs.next()){
                throw new OrderNotFoundException("查询不到任何订单");
            }
            else{
                do{
                    orders.add(getOrderById(rs.getInt(orderId)));
                }while(rs.next());
            }
            return orders;
        } finally {
            JDBCUtil.close(null,null,rs);
        }

    }

    private int getOrdersCount(ResultSet rs) throws Exception{

        int count=0;
        String sql=String.format("select count(%s) count from orders",orderId);
        conn=JDBCUtil.getConnection();
        rs=JDBCUtil.select(conn,sql,null);
        if(rs.next()){
            count = rs.getInt("count");
        }
        else{
            throw new OrderNotFoundException("查询不到订单记录的数量");
        }
        return count;
    }

    private List<Order> selectOrderByPage(ResultSet rs,int begin,int limit) throws Exception{
        String sql=String.format("select %s from orders limit ?,?",ordersAllColumn);
        List<Order> orders=new ArrayList<>();
        conn=JDBCUtil.getConnection();
        rs=JDBCUtil.select(conn,sql,begin,limit);
        if(!rs.next()){
            throw new OrderNotFoundException("分页查询失败");
        }
        else{
            do{
                orders.add(getOrderById(rs.getInt(orderId)));
            }
            while(rs.next());
        }
        return orders;
    }

    public List<Order> getOrdersByPage(int page) throws Exception{
        ResultSet rs=null;
        List<Order> orders=new ArrayList<>();
        try{

            int limit=2;
            int totalCount=getOrdersCount(rs);
            int totalPage=(totalCount%limit!=0) ? totalCount / limit + 1 : totalCount / limit;
            if(page<=0){
                throw new ProductNotFoundException("非法页码！");
            }
            if(page>totalCount){
                throw new ProductNotFoundException("所查询的页码超出最大页数："+totalPage);
            }
            int begin=(page-1)*limit;
            orders=selectOrderByPage(rs,begin,limit);
            return orders;
        } finally {
            JDBCUtil.close(null,null,rs);
        }
    }

    private Item getItemById(ResultSet rs,int id,int itemId) throws Exception{
        String sql=String.format("select %s from order_items where %s = ? and %s = ?",orderItemsAllColumn,orderId,productId);

        rs=JDBCUtil.select(conn,sql,id,itemId);
        if(rs.next()){
            return new Item(rs.getInt(productId),rs.getInt(quantity));
        }
        else{
            throw new OrderNotFoundException("找不到此条记录");
        }
    }
    private void updateItem(Item oldItem,int id,int number) {
        String sql=String.format("update order_items set %s = ? where %s = ? and %s = ?",quantity,orderId,productId);
        JDBCUtil.update(conn,sql,oldItem.getQuantity()+number,id,oldItem.getId());
    }

    private void updateOrderPriceById(int id,int itemId,int number) throws Exception{
        String sql=String.format("update orders set %s = ? where %s = ?",totalPrice,orderId);
        Order order = getOrderById(id);
        Product product = productManagement.getProductById(itemId);
        JDBCUtil.update(conn,sql,order.getTotalPrice()+number*product.getPrice(),id);
    }

    private void deleteItem(int id,int itemId) {
        String sql=String.format("delete from order_items where %s = ? and %s = ?",orderId,productId);
        JDBCUtil.update(conn,sql,id,itemId);
    }




    public int updateOrderById(int id,int itemId,int number)throws Exception{


        ResultSet rs=null;
        ProductManagement productManagement=new ProductManagement();

        try{
            JDBCUtil.startTransaction(conn);
            Item oldItem= getItemById(rs,id,itemId);

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
        finally {
            JDBCUtil.close(null,null,rs);
        }

    }

}
