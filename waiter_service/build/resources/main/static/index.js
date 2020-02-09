//const SERVER_URL = "http://192.168.0.100";
const SERVER_URL = "http://" + window.location.host;
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

function login(){
    const uname = $("#userName").val();
    const pass = $("#password").val();
    if (uname !== "" && pass !== "") {
        const URL = SERVER_URL + "/login?uname=" + uname +
            "&pass=" + pass;

        let xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function () {
            if (this.readyState == 4) {
                if (this.status == 200) {
                    //console.log(this.responseText);

                    let connectedEmp = Object.create(Employee);
                    connectedEmp.init(JSON.parse(this.responseText));

                    if (connectedEmp.job === "CHELNER"){
                        saveResource("connectedEmp", this.responseText);
                        const empUrl = SERVER_URL + "/Restaurant/waiter.html";
                        window.location.replace(empUrl);
                    }
                    else {
                        const html = '<div class="alert alert-danger alert-dismissible">' +
                            '<button type="button" class="close" data-dismiss="alert">&times;</button>' +
                            'Conectare eșuată!' + '</div>';
                        $("#alertArea").append(html);
                        connectedEmp = null;
                    }

                }
                else {
                    console.log(this.responseText);
                    const html = '<div class="alert alert-danger alert-dismissible">' +
                        '<button type="button" class="close" data-dismiss="alert">&times;</button>' +
                        'Conectare eșuată!' + '</div>';
                    $("#alertArea").append(html);
                }
            }
        };
        xhttp.open("GET", URL, true);
        xhttp.send();
    }
    else {
        const html = '<div class="alert alert-warning alert-dismissible">' +
            '<button type="button" class="close" data-dismiss="alert">&times;</button>Completați întreg formularul!</div>';
        $("#alertArea").append(html);
    }
}
function initPage(){
    const emp = JSON.parse(getResource("connectedEmp"));
    if (emp != null){
        const empUrl = SERVER_URL + "/Restaurant/waiter.html";
        if (empUrl != null)
            window.location.replace(empUrl);
    }
}

$(document).ready(function(){
    initPage();

    $("#submit").click(function(){
        login();
    });
});

function saveResource(name,value) {
    window.localStorage.setItem(name, value);
}
function getResource(name) {
    return window.localStorage.getItem(name);
}


