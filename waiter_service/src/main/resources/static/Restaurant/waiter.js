const SERVER_URL = "http://" + window.location.host;

const ALERT_MEAL = "success";
const ALERT_PIZZA = "warning";
const ALERT_DRINK = "primary";
const INTERVAL = 5000; // milliseconds
let LOOP_GET_PREPARED_OPTIONS = true;
let ERROR_OCURRED = false;

let connectedEmp = null;
connectedEmp = JSON.parse(getResource("connectedEmp"));
if (connectedEmp === null) {
    window.location.replace(SERVER_URL);
}
const WAITER_ID = connectedEmp.emp_id;

function createAlert(alertType, message) {
    let alert = '<div class="alert alert-' + alertType + ' alert-dismissible">\n' +
        '<button type="button" class="close" data-dismiss="alert">&times;</button>\n';
    alert += message + "</div>";
    return alert;
}

// *************** Local variables ***************

let menu = Object.create(Menu);
let activeOrders = Object.create(ActiveOrders);
activeOrders.init();


// *************** Methods ***************

function getMenu() {
    $.get(SERVER_URL + "/menu", function (data) {
        menu.init(data);
        menu.mealCategories.sort(); // edited
        menu.meals.sort(); // edited
        const mealCategories = menu.getCategories();
        // mealCategories.sort();
        mealCategories.forEach(function (value) {
            let optionElement = document.createElement("option");
            optionElement.value = value;
            optionElement.innerHTML = value;
            $("#mealType").append(optionElement);
        });
        updateOptions("");
        getBackupOrderList();

        mealCategories.forEach(function (value) {
            let optionElement = document.createElement("option");
            optionElement.value = value;
            optionElement.innerHTML = value;
            $("#editO-mealType").append(optionElement);
        });
        updateOptions("editO-");
    });
}

function getFreeTables(opt) {
    let xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4) {
            if (this.status === 200) {
                document.getElementById(opt + "tableNo").innerHTML = "";
                let data = JSON.parse(this.responseText);
                data.forEach(function (value) {
                    let optionElement = document.createElement("option");
                    optionElement.value = value;
                    optionElement.innerHTML = value;
                    document.getElementById(opt + "tableNo").append(optionElement);
                });
            }
            else {
                $("#alertArea").empty();
                $("#alertArea").append(createAlert("danger", "Serverul nu răspunde!"));
            }
        }
    };
    xhttp.open("GET", SERVER_URL + "/freeTables", false);
    xhttp.send();
}

function getActiveOrders() {
    let xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4) {
            if (this.status === 200) {
                const orders = JSON.parse(this.responseText);
                activeOrders.addAllFromData(orders);
                showActiveOrders();
            }
            else {
                $("#alertArea").empty();
                $("#alertArea").append(createAlert("danger", "Serverul nu răspunde!"));
            }
        }
    };
    xhttp.open("GET", SERVER_URL + "/activeOrders/" + WAITER_ID, false);
    xhttp.send();
}

function showActiveOrders() {
    $("#activeOrders").empty();
    activeOrders.orders.forEach(function (order) {
        let div = '<div id="divTable' + order.tableNumber + '" class="clearfix order">\n';

        let finish = true;
        order.products.forEach(function (product) {
            if (product.status !== ProductStatus.REJECTED && product.isPrepared() === "") {
                finish = false;
            }
        });
        if (finish) {
            div += '<button id="logOutBtn" type="button" class="close" style="color: green" onclick="finishOrder(' +
                order.tableNumber + ')">Finalizare</button>\n';
        }
        div += '<h5>Masa ' + order.tableNumber + '</h5>\n';
        div += '<p>Ora preluării: ' + order.orderDate.getHours() + ":" + order.orderDate.getMinutes() + '</p>\n';
        div += '<button type="button" class="btn btn-link float-left" data-toggle="collapse" ' +
            'data-target="#info' + order.tableNumber + '">Vezi produse</button>\n';
        div += '<button type="button" class="btn btn-link float-right" onclick="editOrder(' + order.tableNumber +
            ')">Editează comanda</button><br><br>';
        div += '<div id="info' + order.tableNumber + '" class="collapse">\n';
        // Insert the products
        order.products.forEach(function (product) {
            if (product.status === ProductStatus.REJECTED) {
                div += '<p>' + product.name + ' - <span style="color: darkred">Respins</span> - ';
                div += product.quantity + ' - ' + product.specs + '</p>';
            } else {
                div += '<p>' + product.name + ' - <span style="color: green">' + product.isPrepared() + '</span> - ';
                div += product.quantity + ' - ' + product.specs + '</p>';
            }
        });
        div += '</div></div><br>';

        $("#activeOrders").append(div);
    });
}

function getPreparedOrders() {
    if (!LOOP_GET_PREPARED_OPTIONS) return;
    if (activeOrders.length !== 0) {
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            if (this.readyState === 4) {
                if (this.status === 200) {
                    if (ERROR_OCURRED) {
                        $("#alertArea").empty();
                        ERROR_OCURRED = false;
                    }
                    const orders = JSON.parse(this.responseText);
                    let hasPreparedOrders = false;

                    orders.forEach(function (order) {
                        hasPreparedOrders = true;
                        const time = new Date(order.orderDate);

                        let message = "<h5>Masa <strong>" + order.tableNumber + "</strong></h5>" +
                            "[<strong>" + time.getHours() + ":" + time.getMinutes() + "</strong>] - "
                            + order.products[0].name + " a/au fost pregătit/ă/e!";

                        if (order.products[0].menuNumber >= 200)
                            $("#preparedOrdersNotificationsArea").append(
                                createAlert(ALERT_DRINK, message));
                        else {
                            if (order.products[0].menuNumber >= 150)
                                $("#preparedOrdersNotificationsArea").append(
                                    createAlert(ALERT_PIZZA, message));
                            else
                                $("#preparedOrdersNotificationsArea").append(
                                    createAlert(ALERT_MEAL, message));
                        }
                    });
                    if (hasPreparedOrders) getActiveOrders();

                } else {
                    $("#alertArea").empty();
                    $("#alertArea").append(createAlert("danger",
                        "Serverul nu răspunde!"));
                    ERROR_OCURRED = true;
                }
            }
        };
        xhttp.open("GET", SERVER_URL + "/preparedOrders/" + WAITER_ID, false);
        xhttp.send();
    }
}

function finishOrder(tableNumber) {
    $.ajax({
        method: "POST",
        url: SERVER_URL + "/newOrder",
        data: JSON.stringify(activeOrders.finishOrder(tableNumber)),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (data) {
            console.log(data);
            getActiveOrders();
            showReceipt(data);
        }
    });
}

function showReceipt(receipt) {
    let receiptResume = "Data: " + receipt.date + "\n";
    receipt.products.forEach(function (product) {
        receiptResume += "" + product.name + " (" + product.price + " lei) - "
            + product.total + " lei\n";
    });
    receiptResume += "Total bon: " + receipt.total + " lei.";
    alert(receiptResume);
}

function sendNewOrder(order) {
    $.ajax({
        type: "POST",
        url: SERVER_URL + "/newOrder",
        data: JSON.stringify(order),
        contentType: "application/json; charset=utf-8",
        dataType: "json",

        statusCode: {
            200: function (xhr) {
                console.log(xhr);
                $("#alertArea").html(createAlert("success", "Comanda pentru masa: " + order.tableNumber +
                    " a fost procesată cu succes!"));
            },
            500: function (xhr) {
                console.log(xhr.responseText);
                $("#alertArea").html(createAlert("danger", "Eroare la procesarea comenzii pentru masa: "
                    + order.tableNumber + "<br/>"));
            }
        }
    });
}

function createNewOrder() {
    const orderList = document.getElementById("orderList").rows;

    let products = [];
    for (let i = 1; i < orderList.length; i++) {
        const cells = orderList[i].cells;
        let product = Object.create(OrderProduct);
        product.init(null, menu.getMealNr(cells[0].innerText), cells[0].innerText,
            parseInt(cells[1].innerText), cells[2].innerText, ProductStatus.NEW);
        products.push(product);
    }

    let newOrder = Object.create(Order);
    newOrder.init();
    newOrder.setNew(OrderType.GENERAL_ORDER, WAITER_ID, 0, parseInt($("#tableNo").val()), products);
    return newOrder;
}

function addProductToOrderList() {
    const produs = $("#meals").val();
    const count = $("#count").val();
    const specs = $('#specs').val();

    let tr = '<tr><td>' + produs + '</td>\n';
    tr += "<td>" + count + "</td>\n";
    tr += "<td>" + specs + "</td>\n";
    tr += "<td><span class='table-edit' onclick='editRow(this)'>Editează</span></td>\n";
    tr += "<td><span class='table-remove' onclick='deleteRow(this)'>Șterge</span></td></tr>";

    $("#orderList").find('tbody').append(tr);

    $("#count").val(1);
    $("#specs").val("-");

    backupNewOrder();
}

function deleteRow(r) {
    var i = r.parentNode.parentNode.rowIndex;
    document.getElementById("orderList").deleteRow(i);
    backupNewOrder();
}

function editRow(r) {
    const i = r.parentNode.parentNode.rowIndex;
    const cells = document.getElementById("orderList").rows[i].cells;

    $("#mealType").val(menu.getMealCategory(cells[0].innerText));
    updateOptions("");
    $("#meals").val(cells[0].innerText);
    $("#count").val(parseInt(cells[1].innerText));
    $("#specs").val(cells[2].innerText);

    document.getElementById("orderList").deleteRow(i);
    backupNewOrder();
}

function resumeNewOrder() {
    const orderList = document.getElementById("orderList").rows;
    let html = "";
    for (let i = 1; i < orderList.length; i++) {
        const cells = orderList[i].cells;
        html += "<p>" + cells[1].innerText + " - " + cells[0].innerText + " - " + cells[2].innerText + "</p>\n";
    }
    $("#orderResume").html(html);
}

function updateOptions(opt) {
    $("#" + opt + "meals").empty();
    let selectedOption = $("#" + opt + "mealType").val();
    let options = menu.getCategoryMeals(selectedOption);

    options.sort((a, b) => (a.getName() > b.getName()) ? 1 : ((b.getName() > a.getName()) ? -1 : 0));
    options.forEach(function (value) {
        let option = document.createElement('option');
        option.value = value.getName();
        option.innerHTML = value.getName();
        $("#" + opt + "meals").append(option);
    })
}

function clearOrderForm() {

    $("#mealType").val(menu.mealCategories[0]);
    updateOptions("");
    $("#count").val(1);
    $("#specs").val("-");

    const orderList = document.getElementById("orderList").rows;
    for (let i = orderList.length - 1; i >= 1; i--) {
        document.getElementById("orderList").deleteRow(i);
    }

    getFreeTables("");
    deleteResource("orderList");
    deleteResource("newProductForm");
    resumeNewOrder();
}

function backupNewOrder() {

    const html = document.getElementById("orderList").innerHTML;
    saveResource("orderList", html);

    const product = document.getElementById("newProductForm").innerHTML;
    saveResource("newProductForm", product);
}

function getBackupOrderList() {
    const product = getResource("newProductForm");
    if (product !== null)
        $("#newProductForm").html(product);

    const html = getResource("orderList");
    if (html !== null)
        $("#orderList").html(html);
}

function initPage() {
    getMenu();
    getActiveOrders();

    $("#logInfo").append('Autentificat ca <strong>' + connectedEmp.name + '</strong>');

    setInterval(getPreparedOrders, INTERVAL);
}

// *************** Edit order ***************
let updateOrder = null;
let type = "new";
let editedProductIndex = -1;
let editedProduct = null;

function editOrder(tableNumber) {

    LOOP_GET_PREPARED_OPTIONS = false;
    const order = activeOrders.getOrderByTableNumber(tableNumber);
    showEditOrder(order);

    updateOrder = Object.create(Order);
    updateOrder.init();
    updateOrder.setNew(OrderType.UPDATE_ORDER, WAITER_ID, order.orderId, tableNumber, []);
    $("#editOrder").show();
}

function showEditOrder(order) {
    getFreeTables("editO-");

    // Adaug si numarul de masa din comanda prelucrata
    let optionElement = document.createElement("option");
    optionElement.value = order.tableNumber;
    optionElement.innerHTML = order.tableNumber;
    $("#editO-tableNo").append(optionElement);

    $("#editO-tableNo").val(order.tableNumber);
    $("#editO-mealType").val(menu.mealCategories[0]);
    updateOptions("editO-");
    $("#editO-count").val(1);
    $("#editO-specs").val("-");


    $("#editOrderList").find('tbody').html("");
    order.products.forEach(function (product) {
        let tr = "";

        switch (product.status) {
            case ProductStatus.REJECTED:
                break;
            case ProductStatus.PREPARED:
                tr += '<tr><td>' + product.name + '</td>\n';
                tr += "<td>" + product.quantity + "</td>\n";
                tr += "<td>" + product.specs + "</td>\n";
                tr += "<td><span style='color: green'>Pre parat</span></td>";
                tr += "<td><span class='table-remove' onclick='rejectProduct(this, " + product.id + ")'>Respins</span></td></tr>";
                break;
            case ProductStatus.NEW:
            case ProductStatus.UPDATED:
                tr += '<tr><td>' + product.name + '</td>\n';
                tr += "<td>" + product.quantity + "</td>\n";
                tr += "<td>" + product.specs + "</td>\n";
                tr += "<td><span class='table-edit' onclick='editOrderProduct(this, " + product.id + ", \"old\")'>Editează</span></td>\n";
                tr += "<td><span class='table-remove' onclick='deleteOrderProduct(this, " + product.id + ", \"old\")'>Șterge</span></td></tr>";
                break;
            default:
                break;
        }

        if (tr !== "")
            $("#editOrderList").find('tbody').append(tr);
    });
}

function editOrderProduct(row, prodId, t) {
    if (type === "old")
        return;

    if (t === "new") {
        const rowIndex = row.parentNode.parentNode.rowIndex;
        const product = updateOrder.deleteProduct(prodId);

        $("#editO-mealType").val(menu.getMealCategory(product.name));
        updateOptions("editO-");
        $("#editO-meals").val(product.name);
        $("#editO-count").val(product.quantity);
        $("#editO-specs").val(product.specs);

        document.getElementById("editOrderList").deleteRow(rowIndex);
    } else if (t === "old") {
        editedProductIndex = row.parentNode.parentNode.rowIndex;
        type = "old";
        let tableNumber = updateOrder.tableNumber;
        editedProduct = activeOrders.getOrderByTableNumber(tableNumber).getProduct(prodId);

        $("#editO-mealType").val(menu.getMealCategory(editedProduct.name)).prop('disabled', true);
        updateOptions("editO-");
        $("#editO-meals").val(editedProduct.name).prop('disabled', true);

        $("#editO-count").val(editedProduct.quantity);
        $("#editO-specs").val(editedProduct.specs);
    }

}

function deleteOrderProduct(row, prodId, t) {
    const i = row.parentNode.parentNode.rowIndex;
    if (t === "new") {
        document.getElementById("editOrderList").deleteRow(i);
        updateOrder.deleteProduct(prodId);
    } else if (t === "old") {
        let deletedProduct = Object.create(OrderProduct);

        let name = document.getElementById("editOrderList").rows[i].cells[0].innerText;
        let menuNr = menu.getMealNr(name);

        deletedProduct.init(prodId, menuNr, "", 0, "", ProductStatus.DELETED);
        updateOrder.products.push(deletedProduct);

        const list = document.getElementById("editOrderList");
        list.rows[i].cells[3].innerHTML = '<span style="color: darkred">Șters</span>';
        list.rows[i].cells[4].innerHTML = "<span class='table-edit' onclick='undoD(this, " + prodId + ")'>Undo</span>";
    }
}

function undoD(row, prodId) {
    const i = row.parentNode.parentNode.rowIndex;
    updateOrder.undoDeleteOrReject(prodId);

    const list = document.getElementById("editOrderList");
    list.rows[i].cells[3].innerHTML = "<span class='table-edit' onclick='editOrderProduct(this, " + prodId + ", \"old\")'>Editează</span>";
    list.rows[i].cells[4].innerHTML = "<span class='table-remove' onclick='deleteOrderProduct(this, " + prodId + ", \"old\")'>Șterge</span>";
}

function rejectProduct(row, prodId) {
    const i = row.parentNode.parentNode.rowIndex;

    let rejectedProduct = Object.create(OrderProduct);
    rejectedProduct.init(prodId, 0, "", 0, "", ProductStatus.REJECTED);
    updateOrder.products.push(rejectedProduct);

    const list = document.getElementById("editOrderList");
    list.rows[i].cells[3].innerHTML = '<span style="color: darkred">Respins</span>';
    list.rows[i].cells[4].innerHTML = "<span class='table-edit' onclick='undoR(this, " + prodId + ")'>Undo</span>";
}

function undoR(row, prodId) {
    const i = row.parentNode.parentNode.rowIndex;
    updateOrder.undoDeleteOrReject(prodId);

    const list = document.getElementById("editOrderList");
    list.rows[i].cells[3].innerHTML = "<span style='color: green'>Pre parat</span>";
    list.rows[i].cells[4].innerHTML = "<span class='table-remove' onclick='rejectProduct(this, " + prodId + ")'>Respins</span>";
}

function addNewProduct() {
    // Creez un nou produs obiect
    let newProduct = Object.create(OrderProduct);
    const name = $("#editO-meals").val();
    const mealNr = menu.getMealNr(name);
    const qty = parseInt($("#editO-count").val());
    newProduct.init(null, mealNr, name, qty, $("#editO-specs").val(), ProductStatus.NEW);

    $("#editO-count").val(1);
    $("#editO-specs").val("-");

    // Il afisez in list
    let tr = '<tr><td>' + newProduct.name + '</td>\n';
    tr += "<td>" + newProduct.quantity + "</td>\n";
    tr += "<td>" + newProduct.specs + "</td>\n";
    tr += "<td><span class='table-edit' onclick='editOrderProduct(this, " + newProduct.id + ", \"new\")'>Editează</span></td>\n";
    tr += "<td><span class='table-remove' onclick='deleteOrderProduct(this, " + newProduct.id + ", \"new\")'>Șterge</span></td></tr>";
    $("#editOrderList").find('tbody').append(tr);

    // Il adaug in updateOrder
    updateOrder.products.push(newProduct);
}

function addEditedProduct() {
    const qty = parseInt($("#editO-count").val());
    const specs = $("#editO-specs").val();

    if (editedProduct.quantity !== qty || editedProduct.specs !== specs) {
        // Creez un produs de tip update
        let updatedProduct = Object.create(OrderProduct);
        let menuNumber = menu.getMealNr($("#editO-meals").val());
        updatedProduct.init(editedProduct.id, menuNumber, "", qty, specs, ProductStatus.UPDATED);
        updateOrder.products.push(updatedProduct);

        const i = editedProductIndex;

        let list = document.getElementById("editOrderList");
        list.rows[i].cells[1].innerHTML = qty.toString();
        list.rows[i].cells[2].innerHTML = $("#editO-specs").val();
    }

    type = "new";
    editedProduct = null;
    editedProductIndex = -1;
    $("#editO-mealType").prop('disabled', false);
    $("#editO-meals").prop('disabled', false);
    $("#editO-count").val(1);
    $("#editO-specs").val("-");
}

function sendUpdateOrder(order) {
    $.ajax({
        method: "PUT",
        url: SERVER_URL + "/updateOrder",
        data: JSON.stringify(order),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (data) {
            console.log(data);
        }
    });
}


// *************** Events Listener ***************

$(document).ready(function () {

    initPage();

    $("#refreshActiveOrders").click(function () {
        getActiveOrders();
        LOOP_GET_PREPARED_OPTIONS = true;
        getPreparedOrders();
    });

    // ******** New Order ********
    $("#logOut").click(function () {
        deleteResource("connectedEmp");
        window.location.href = SERVER_URL;
    });

    $("#newOrderBtn").click(function () {
        getFreeTables("");
    });

    $("#mealType").change(function () {
        updateOptions("");
    });

    $("#addProductBtn").click(function () {
        addProductToOrderList();
        resumeNewOrder();
    });

    $("#orderSendBtn").click(function () {
        resumeNewOrder();
    });

    $("#orderConfirmationBtn").click(function () {
        sendNewOrder(createNewOrder());
        setTimeout(getActiveOrders, 300);
        setTimeout(clearOrderForm, 300);
        $("#newOrderBtn").click();
    });

    $("#clearOrderFormBtn").click(function () {
        clearOrderForm();
    });

    // ****** Edit Order Events Listeners
    $("#editO-addProductBtn").click(function () {
        if (type === "new")
            addNewProduct();
        else if (type === "old")
            addEditedProduct();
    });

    $("#editO-mealType").change(function () {
        updateOptions("editO-");
    });

    $("#editOrderConfirmationBtn").click(function () {
        const tableNumber = parseInt($("#editO-tableNo").val());
        if (tableNumber !== updateOrder.tableNumber)
            updateOrder.tableNumber = tableNumber;
        console.log(updateOrder.toString());

        sendUpdateOrder(updateOrder);
        updateOrder = null;
        $("#editOrder").hide();
        LOOP_GET_PREPARED_OPTIONS = true;

        setTimeout(getActiveOrders, 500);
        setTimeout(showActiveOrders, 700);
    });

    $("#hideEditOrderBtn").click(function () {
        $("#editOrder").hide();
        LOOP_GET_PREPARED_OPTIONS = true;
    });

});
