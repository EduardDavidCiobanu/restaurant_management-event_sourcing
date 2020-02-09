// *************** Object declarations ***************

const ProductStatus = {
    NEW: "NEW",
    UPDATED: "UPDATE",
    DELETED: "DELETED",
    PREPARED: "PREPARED",
    REJECTED: "REJECTED"
};
let OrderProduct = {
    init: function (id, menuNumber, name, quantity, specs, status){
        if (id === null)
            this.id = Math.floor(Math.random() * (2147483647 - 1 + 1) ) + 1;
        else
            this.id = id;
        this.menuNumber = menuNumber;
        this.name = name;
        this.quantity = quantity;
        this.specs = specs;
        this.status = status;
    },
    setPrepared: function () {
        this.status = ProductStatus.PREPARED;
    },
    equals: function (data) {
        return data.id === this.id;
    }

};
const OrderType = {
    GENERAL_ORDER: "GENERAL_ORDER",
    UPDATE_ORDER: "UPDATE_ORDER",
    DELETE_ORDER: "DELETE_ORDER",
    PIZZA_PREPARED: "PIZZA_PREPARED",
    MEAL_PREPARED: "MEAL_PREPARED",
    DRINK_PREPARED: "DRINK_PREPARED"
};
let Order = {

    init: function(){
        this.orderType = null;
        this.orderDate = null;
        this.tableNumber = null;
        this.orderId = null;
        this.parentOrderId = 0;
        this.waiterId = null;
        this.products = [];
    },

    setFromData: function(data){
        this.orderType = data.orderType;
        this.orderDate = new Date(data.orderDate).getTime();
        this.tableNumber = data.tableNumber;
        this.orderId = data.orderId;
        this.parentOrderId = data.parentOrderId;
        this.waiterId = data.waiterId;

        for (let i = 0; i < data.products.length; i++){
            const p = data.products[i];
            let product = Object.create(OrderProduct);
            product.init(p.id, p.menuNumber, p.name, p.quantity, p.specs, p.status);
            this.products.push(product);
        }
    },

    setNew: function(orderType, tableNo, parentId, waiterId, products){
        this.orderType = orderType;
        this.orderDate = new Date().getTime();
        this.tableNumber = tableNo;
        this.orderId = Math.floor(Math.random() * (2147483647 - 1 + 1) ) + 1;
        this.parentOrderId = parentId;
        this.waiterId = waiterId;
        this.products = products;
    }
};
let ActiveOrders = {
    init: function (){
        this.orders = [];
        this.length = 0;
    },
    addAllFromData: function (data){
        this.orders = [];
        for (let i = 0; i < data.length; i++) {
            let order = Object.create(Order);
            order.init();
            order.setFromData(data[i]);
            this.orders.push(order);
        }
        this.length = this.orders.length;
    },
    getOrderByTableNumber: function (tableNumber){
        let findOrder = null;
        this.orders.forEach(function (order){
            if(order.tableNumber === tableNumber) {
                findOrder = order;
            }
        });
        return findOrder;
    }
};
let Employee = {
    init: function (data){
        this.emp_id = data.emp_id;
        this.name = data.name;
        this.job = data.job;
        this.hire_date = data.hire_date;
        this.account_name = data.account_name;
        this.password = data.password;
    }
};


function saveResource(name,value) {
    window.localStorage.setItem(name, value);
}
function getResource(name) {
    return window.localStorage.getItem(name);
}
function deleteResource(name) {
    window.localStorage.removeItem(name);
}