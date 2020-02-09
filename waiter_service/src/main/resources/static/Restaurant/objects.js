// *************** Object declarations ***************

let Meal = {
    init: function (nr, category, name, info, price) {
        this.nr = nr;
        this.category = category;
        this.name = name;
        this.info = info;
        this.price = price;
    },

    getName: function () {
        return this.name;
    },
    getNr: function () {
        return this.nr;
    },
    getCategory: function () {
        return this.category;
    }
};
let Menu = {

    init: function (data) {
        this.meals = [];
        this.mealCategories = [];

        for (let i = 0; i < data.length; i++) {
            let meal = Object.create(Meal);
            meal.init(data[i].nr, data[i].category, data[i].name, data[i].info, data[i].price);
            this.meals.push(meal);

            if (!this.mealCategories.includes(data[i].category)) {
                this.mealCategories.push(data[i].category);
            }
        }
    },

    getCategories: function () {
        return this.mealCategories;
    },
    getCategoryMeals: function (category) {
        let categoryMeals = [];
        for (let i = 0; i < this.meals.length; ++i) {
            if (this.meals[i].getCategory() === category) {
                categoryMeals.push(this.meals[i]);
            }
        }

        return categoryMeals;
    },
    getMealNr: function (name) {
        for (let i = 0; i < this.meals.length; i++) {
            if (this.meals[i].getName() === name)
                return this.meals[i].getNr();
        }
    },
    getMealCategory: function (name) {
        for (let i = 0; i < this.meals.length; i++) {
            if (this.meals[i].getName() === name)
                return this.meals[i].getCategory();
        }
    }
};

const ProductStatus = {
    NEW: "NEW",
    UPDATED: "UPDATED",
    DELETED: "DELETED",
    PREPARED: "PREPARED",
    REJECTED: "REJECTED"
};
var OrderProduct = {
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
    isPrepared: function () {if (this.status === ProductStatus.PREPARED) return "PREPARAT"; else return "";},
    setPrepared: function (data) {
        if(this.equals(data)) {
            this.status = ProductStatus.PREPARED;
            return true;
        }
        return false;
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
    MEAL_PREAPARED: "MEAL_PREPARED",
    DRINK_PREPARED: "DRINK_PREPARED",
    FINISHED_ORDER: "FINISHED_ORDER"
};
var Order = {

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
        this.orderDate = new Date(data.orderDate);
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

    setNew: function(orderType, waiterId, parentOrderId, tableNo, products){
        this.orderType = orderType;
        this.orderDate = new Date().getTime();
        this.tableNumber = tableNo;
        this.orderId = Math.floor(Math.random() * (2147483647 - 1 + 1) ) + 1;
        this.parentOrderId = parentOrderId;
        this.waiterId = waiterId;
        this.products = products;
    },

    /**
     *      Search for the prepared product, mark it as prepared, and then alert the waiter about this prepared product.
     * @param data the product prepared
     */
    setPreparedProduct: function(data){
        for (let i = 0; i < this.products.length; i++){
            if(this.products[i].setPrepared(data)){
                break;
            }
        }
    },

    getProduct: function(productId) {
        const i = this.getIndexOfProduct(productId);
        if (i === -1)
            return null;
        return this.products[i];
    },

    getIndexOfProduct: function(productId) {
        for(let i = 0; i < this.products.length; i++){
            if(this.products[i].id === productId){
                return i;
            }
        }
        return -1;
    },

    deleteProduct: function(productId) {
        let i;
        for(i = 0; i < this.products.length; i++){
            if(this.products[i].id === productId){
                break;
            }
        }
        const result = this.products[i];
        if(i === this.products.length - 1)
            this.products.pop();
        else {
            if (i >= this.products.length)
                return null;
            else {
                while(i+1 < this.products.length){
                    this.products[i] = this.products[i+1];
                    i++;
                }
                this.products.pop();
            }
        }
        return result;
    },

    undoDeleteOrReject: function(productId){
        let i;
        for(i = 0; i < this.products.length; i++){
            if(this.products[i].id === productId &&
                (this.products[i].status === ProductStatus.DELETED ||
                    this.products[i].status === ProductStatus.REJECTED)){
                break;
            }
        }
        if(i === this.products.length - 1)
            this.products.pop();
        else {
            if (i >= this.products.length)
                return "";
            else {
                while(i+1 < this.products.length){
                    this.products[i] = this.products[i+1];
                    i++;
                }
                this.products.pop();
            }
        }
        return "";
    }
};
var ActiveOrders = {
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
    setPreparedOrders: function (data){
        for (let i = 0; i < data.length; i++) {
            const products = data[i].products;
            for (let j = 0; j < products.length; j++) {
                if (this.length > 0) {
                    this.orders.forEach(function (order) {
                        if (order.orderId === data[i].parentOrderId) {
                            order.setPreparedProduct(products[j]);
                        }
                    });
                }
            }
        }
    },
    getOrderByTableNumber: function (tableNumber){
        let findOrder = null;
        this.orders.forEach(function (order){
            if(order.tableNumber === tableNumber) {
                findOrder = order;
            }
        });
        return findOrder;
    },
    deleteOrder(tableNumber){
        const deletedOrder = this.getOrderByTableNumber(tableNumber);
        let i = this.orders.indexOf(deletedOrder);

        if ( i === this.orders.length - 1)
            this.orders.pop();
        else {
            while(i+1 < this.orders.length){
                this.orders[i] = this.orders[i+1];
                i++;
            }
            this.orders.pop();
        }
        return deletedOrder;
    },
    finishOrder(tableNumber){
        let finishedOrder = Object.create(Order);
        finishedOrder.init();
        const oldOrder = this.deleteOrder(tableNumber);
        finishedOrder.setNew(OrderType.FINISHED_ORDER, oldOrder.waiterId, oldOrder.orderId, tableNumber, oldOrder.products);
        return finishedOrder;
    }
};
var Employee = {
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