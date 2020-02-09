let connectedEmp = Object.create(Employee);
let activeOrders = Object.create(ActiveOrders);
activeOrders.init();
const SERVER_URL = "http://" + window.location.host;
let COOKER_URL = "";
let ORDER_TYPE = "";
let LOOP = false;
const INTERVAL = 5000;
let ERROR_OCURRED = false;


// **************** Methods ****************
function initPage() {
    let resource = getResource("connectedEmp");
    if (resource === null)
        window.location.replace(SERVER_URL);
    else
        connectedEmp.init(JSON.parse(resource));

    let empInfo = "";
    switch (connectedEmp.job) {
        case "BUCATAR":
            COOKER_URL = SERVER_URL + "/generalCooker";
            ORDER_TYPE = OrderType.MEAL_PREPARED;
            empInfo = '<h4>Pagina Bucătarului</h4>\n<p>Autentificat ca <strong>' + connectedEmp.name + '</strong></p>';
            break;
        case "PIZZETAR":
            COOKER_URL = SERVER_URL + "/pizzaCooker";
            ORDER_TYPE = OrderType.PIZZA_PREPARED;
            empInfo = '<h4>Pagina Pizzetarului</h4>\n<p>Autentificat ca <strong>' + connectedEmp.name + '</strong></p>';
            break;
        case "BARMAN":
            COOKER_URL = SERVER_URL + "/barman";
            ORDER_TYPE = OrderType.DRINK_PREPARED;
            empInfo = '<h4>Pagina Barmanului</h4>\n<p>Autentificat ca <strong>' + connectedEmp.name + '</strong></p>';
            break;
        default:
            break;
    }
    $("#empInfo").append(empInfo);
    LOOP = true;
    getActiveOrders();
    setInterval(getActiveOrders, INTERVAL);
    setInterval(function () { $("#alertArea").empty()}, 10000);
    setInterval(checkKitchenService, 5000);
}

function getActiveOrders() {
    if (LOOP) {
        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            if (this.readyState === 4) {
                if (this.status === 200) {
                    if (ERROR_OCURRED) {
                        $("#alertArea").empty();
                        ERROR_OCURRED = false;
                    }

                    let ordersReceived = JSON.parse(this.responseText);

                    if (ordersReceived.length > 0) {
                        activeOrders.addAllFromData(ordersReceived);

                        $("#orderList").empty();
                        let thead = "<thead><tr><th>Produs</th><th>Cantitate</th>" +
                            "<th>Specificații</th><th>Ora</th><th></th></tr></thead>/n<tbody>";
                        $("#orderList").append(thead);

                        activeOrders.orders.forEach(function (order) {
                            order.products.forEach(function (product) {
                                if (product.status === ProductStatus.NEW || product.status === ProductStatus.UPDATED) {
                                    const time = new Date(order.orderDate);

                                    let tr = '<tr><td>' + product.name + '</td>\n';
                                    tr += '<td>' + product.quantity + '</td>\n';
                                    tr += '<td>' + product.specs + '</td>\n';
                                    tr += '<td>' + time.getHours() + ':' + time.getMinutes() + '</td>\n';
                                    tr += '<td><button class="btn btn-success" onclick="prepare(this,' +
                                        order.tableNumber + ', ' + order.products.indexOf(product) + ')">Prepară</button></td></tr>';

                                    $("#orderList").append(tr);
                                }
                            });
                        });
                        $("#orderList").append("</tbody>");
                    } else {
                        $("#orderList").empty();
                    }
                } else {
                    $("#alertArea").empty();
                    $("#alertArea").append(createAlert("danger", "Serverul nu răspunde!"));
                    ERROR_OCURRED = true;
                }
            }
        };
        xhttp.open("GET", COOKER_URL + "/activeOrders", true);
        xhttp.send();
    }
}

function prepare(cell, tableNumber, productIndex) {
    LOOP = false;

    const order = activeOrders.getOrderByTableNumber(tableNumber);
    let product = order.products[productIndex];
    product.setPrepared();
    let products = [];
    products.push(product);

    let newOrder = Object.create(Order);
    newOrder.init();
    newOrder.setNew(ORDER_TYPE, tableNumber, order.parentOrderId, order.waiterId, products);

    let xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4) {
            if (this.status === 200) {
                const i = cell.parentNode.parentNode.rowIndex;
                document.getElementById("orderList").deleteRow(i);

                $("#alertArea").append(createAlert("success",
                    "Chelnerul este informat despre prepararea produsului " + product.name + "."));
            } else {
                $("#alertArea").append(createAlert("danger",
                    "Eroare la notificarea chelnerul despre prepararea produsului" + product.name + "."));
                newOrder = null;
            }
        }
    };
    xhttp.open("POST", COOKER_URL + "/preparedOrder", true);
    xhttp.send(JSON.stringify(newOrder));

    LOOP = true;
}

function checkKitchenService(){
    let xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState === 4) {
            if (this.status === 200){
                if (this.responseText === "FAILED") {
                    $("#alertArea").empty();
                    $("#alertArea").append(createAlert("danger",
                        "Eroare la server! Comandă de la chelner nelivrată!"));
                    ERROR_OCURRED = true;
                    LOOP = false;
                } else if (this.responseText === "DONE") {
                    if (ERROR_OCURRED) {
                        $("#alertArea").empty();
                        ERROR_OCURRED = false;
                        LOOP = true;
                        getActiveOrders();
                    }
                }
            }
        }
    };
    xhttp.open("GET", COOKER_URL + "/checkKitchenService");
    xhttp.send();
}

function createAlert(alertType, message) {
    let alert = '<div class="alert alert-' + alertType + ' alert-dismissible">\n' +
        '<button type="button" class="close" data-dismiss="alert">&times;</button>\n';
    alert += message + "</div>";
    return alert;
}
// **************** Event listener ****************
$(document).ready(function () {
    initPage();

    $("#logOutBtn").click(function () {
        deleteResource("connectedEmp");
        connectedEmp = null;
        window.location.replace(SERVER_URL);
    })
});

