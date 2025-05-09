# **第三轮考核学习心得**



- ## 学习内容


- MySQL基础内容

- JDBC的简单使用

- Maven的简单使用

- ## 建表



- ### 商品表

```mysql
CREATE TABLE products (
                          product_id INT AUTO_INCREMENT PRIMARY KEY,
                          product_name VARCHAR(100) NOT NULL UNIQUE,  -- 唯一约束确保商品名不重复
                          price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
                          stock INT NOT NULL CHECK (stock >= 0)
);
```

- ### 订单表

```mysql
CREATE TABLE orders (
                        order_id INT AUTO_INCREMENT PRIMARY KEY,
                        order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                        total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0)

);
```

- ### 商品-订单表

  - 生成一张中间表，实现order和product的一对多
  - 同时根据order_id删除order表中的某一项时会级联删除该表中具有相同id的元组
  - 禁止删除被该表记录的product

```mysql
CREATE TABLE order_items (
                             order_id INT NOT NULL,
                             product_id INT NOT NULL,
                             quantity INT NOT NULL CHECK (quantity > 0),
                             PRIMARY KEY (order_id, product_id),
                             FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
                             FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT
);
```

- ## 功能实现（重要？）


- ### 解决SQL注入问题
  - 使用预编译的SQL语句和参数化的查询PreparedStatement，避免直接拼接参数

- ### 事务管理
  - 在方法使用时，遇到非法操作就抛出相应的异常，捕获到异常后就回滚事务

- ### 异常处理
  - 自定义了一些异常类，遇到非法操作时，或执行查询，更新语句出现异常，抛出这些异常的具体信息
  - 在测试类中捕获对应的异常，再进行相应的处理

- ### 插入，删除订单
  - 插入订单时，会检查商品是否存在，订单总价是否合法，商品库存是否足够。
  - 删除订单时，会级联删除订单-商品表中的相应的项

- ### 根据id查询订单和产品
  - 未查询到抛出具体的异常信息

- ### 更新商品
  - 传入Product对象和更新的数量
  - 检查更新的数量是否合法，是否超出库存

- ### 商品和订单的分页查询
  - 根据传入的页码返回查询结果
  - 判断页码是否合法，是否超出总页数

- ### 更新订单
  - 更新订单中某个商品的数量，同时将商品返回给库存
  - 如果某项商品的数量被更新为0，则在order_items表中删除该项

- ## 项目结构

```
\ProductManagement
│  .gitignore
│  pom.xml
|  README.md
│  
+---.idea
│   ....
│      
+---src
│  +---main
│  │  +---java
│  │  │   \---org
│  │  │       \---Lskar
│  │  │           \---ProductManagement
│  │  │               +---domain
│  │  │               │       Item.java
│  │  │               │       Order.java
│  │  │               │       Product.java
│  │  │               │      
│  │  │               +---order
│  │  │               │       OrderManagement.java
│  │  │               │      
│  │  │               +---product
│  │  │               │       ProductManagement.java
│  │  │               │      
│  │  │               +---utils
│  │  │                       ProductNotFoundException.java
│  │  │                       OrderNotFoundException.java
│  │  │                       ErrorInQueryException.java
│  │  │                       ErrorInUpdateException.java
│  │  │                       IllegalPriceException.java
│  │  │                       IllegalStockException.java
│  │  │                       JDBCUtil.java
│  │  │                      
│  │  \---resources
│  │          driver.properties
│  │          
│  \---test
│      +---java
│      │      TestClass.java
│      │      
│      \---resources
\---target
    ......
```

- ## 问题
  - 项目结构不知道怎么规范写
  - 方法写比较乱，出现错误的时候改动的比较多
  - 在JDBCUtil里面，ps未释放，因为释放后返回的ResultSet对象也会被释放，导致在其他方法里无法使用






