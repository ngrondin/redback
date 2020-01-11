(window["webpackJsonp"] = window["webpackJsonp"] || []).push([["main"],{

/***/ "./$$_lazy_route_resource lazy recursive":
/*!******************************************************!*\
  !*** ./$$_lazy_route_resource lazy namespace object ***!
  \******************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function webpackEmptyAsyncContext(req) {
	// Here Promise.resolve().then() is used instead of new Promise() to prevent
	// uncaught exception popping up in devtools
	return Promise.resolve().then(function() {
		var e = new Error("Cannot find module '" + req + "'");
		e.code = 'MODULE_NOT_FOUND';
		throw e;
	});
}
webpackEmptyAsyncContext.keys = function() { return []; };
webpackEmptyAsyncContext.resolve = webpackEmptyAsyncContext;
module.exports = webpackEmptyAsyncContext;
webpackEmptyAsyncContext.id = "./$$_lazy_route_resource lazy recursive";

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/app.component.html":
/*!**************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/app.component.html ***!
  \**************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<desktop-root \r\n    *ngIf=\"type == 'desktop'\"\r\n    [title] = \"title\"\r\n    [initialView] = \"initialView\"\r\n    [menuView]=\"menuView\"\r\n    [version]=\"version\">\r\n</desktop-root>"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/desktop-root/desktop-root.component.html":
/*!************************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/desktop-root/desktop-root.component.html ***!
  \************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<mat-toolbar\n    color=\"primary\">\n    <mat-toolbar-row>\n        <span>{{title}}</span>\n    </mat-toolbar-row>\n</mat-toolbar>\n<mat-sidenav-container>\n    <mat-sidenav \n        mode=\"side\" \n        opened>\n        <rb-view-loader \n            [src]=\"menuUrl\"\n            (navigate)=\"navigateTo($event)\">\n        </rb-view-loader>    \n    </mat-sidenav>\n    <mat-sidenav-content>\n        <rb-view-loader \n            [src]=\"viewUrl\"\n            (navigate)=\"navigateTo($event)\">\n        </rb-view-loader>\n    </mat-sidenav-content>\n</mat-sidenav-container>\n<div\n    class=\"rb-footer\">\n</div>\n"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/rb-datetime-input/rb-datetime-input.component.html":
/*!**********************************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/rb-datetime-input/rb-datetime-input.component.html ***!
  \**********************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<mat-form-field\n    [style.width]=\"(size * 15) + 'px'\" >\n    <mat-label>{{label}}</mat-label>\n    <input \n        #input\n        matInput \n        [(ngModel)]=\"displayvalue\"\n        (focus)=\"focus()\"\n        (blur)=\"blur()\"\n        (keydown)=\"keydown($event)\"\n        [readonly]=\"readonly\">\n    <mat-icon \n        matPrefix\n        class=\"rb-grey-icon\">{{icon}}</mat-icon>\n</mat-form-field\n>"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/rb-duration-input/rb-duration-input.component.html":
/*!**********************************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/rb-duration-input/rb-duration-input.component.html ***!
  \**********************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<mat-form-field\n    [style.width]=\"(size * 15) + 'px'\" >\n    <mat-label>{{label}}</mat-label>\n    <input \n        #input\n        matInput \n        [(ngModel)]=\"displayvalue\"\n        (change)=\"commit()\"\n        (focus)=\"focus()\"\n        (blur)=\"blur()\"\n        (keydown)=\"keydown($event)\"\n        [readonly]=\"readonly\">\n    <mat-icon \n        matPrefix\n        class=\"rb-grey-icon\">{{icon}}</mat-icon>\n</mat-form-field\n>"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/rb-input/rb-input.component.html":
/*!****************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/rb-input/rb-input.component.html ***!
  \****************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<mat-form-field \n    [style.width]=\"(size * 15) + 'px'\" >\n    <mat-label>{{label}}</mat-label>\n    <input \n        matInput \n        [(ngModel)]=\"value\"\n        (change)=\"commit()\"\n        [readonly]=\"readonly\">\n    <mat-icon \n        matPrefix\n        class=\"rb-grey-icon\">{{icon}}</mat-icon>\n</mat-form-field>"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/rb-map/rb-map.component.html":
/*!************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/rb-map/rb-map.component.html ***!
  \************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<p>rb-map works!</p>\n"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/rb-popup-datetime/rb-popup-datetime.component.html":
/*!**********************************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/rb-popup-datetime/rb-popup-datetime.component.html ***!
  \**********************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<div\n    *ngIf=\"currentPart == 0\">\n    <div\n        class=\"rb-datetime-cal-header\">\n        <div\n            class=\"rb-datetime-cal-monthnav\"\n            (click)=\"previousMonth()\">\n            <span>&lt;</span>\n        </div>\n        <div\n            class=\"rb-datetime-cal-title\">\n            <span>{{monthsOfYear[month]}} {{year}}</span>\n        </div>\n        <div\n            class=\"rb-datetime-cal-monthnav\"\n            (click)=\"nextMonth()\">\n            <span>&gt;</span>\n        </div>\n    </div>\n    <div\n        class=\"rb-datetime-cal-weekline\">\n        <div\n            *ngFor=\"let d of daysOfWeek\"\n            class=\"rb-datetime-cal-dayheader\"\n            style=\"color: #bbbbbb;\">\n            <span>{{d}}</span>\n        </div>\n    </div>\n    <div\n        *ngFor=\"let w of calendar\"\n        class=\"rb-datetime-cal-weekline\">\n        <div\n            *ngFor=\"let d of w\">\n            <div\n                *ngIf=\"d == ''\"\n                class=\"rb-datetime-cal-emptydaybox\">\n            </div>\n            <div\n                *ngIf=\"d != ''\"\n                class=\"rb-datetime-cal-daybox\"\n                [style.background-color]=\"day == d ? 'rgb(255,64,129)' : 'transparent'\"\n                [style.color]=\"day == d ? '#ffffff' : '#666666'\"\n                (click)=\"selectDate(d)\">\n                <span>{{d}}</span>\n            </div>            \n        </div>   \n    </div>\n</div>\n<div\n    *ngIf=\"currentPart == 1\">\n    <div \n        class=\"rb-datetime-clock-back\"\n        (click)=\"selectHour($event)\">\n        <div \n            class=\"rb-datetime-clock-dial-container\"\n            [style.transform]=\"'rotate(' + (15 * hour) + 'deg)'\">\n            <div \n                class=\"rb-datetime-clock-dial-pointer\"></div>\n        </div>\n        <div \n            *ngFor=\"let t of hoursOfDay; let i = index\"\n            class=\"rb-datetime-clock-dial-container\"\n            [style.transform]=\"'rotate(' + (30 * i) + 'deg)'\">\n            <div \n                class=\"rb-datetime-clock-dial-number\"\n                [style.transform]=\"'rotate(-' + (30 * i) + 'deg)'\">\n            {{t}}\n            </div>\n        </div>  \n    </div>\n</div>\n<div\n    *ngIf=\"currentPart == 2\">\n    <div \n        class=\"rb-datetime-clock-back\"\n        (click)=\"selectMinute($event)\">\n        <div \n            class=\"rb-datetime-clock-dial-container\"\n            [style.transform]=\"'rotate(' + (15 * minute) + 'deg)'\">\n            <div \n                class=\"rb-datetime-clock-dial-pointer\"></div>\n        </div>\n        <div \n            *ngFor=\"let t of minutesOfHour; let i = index\"\n            class=\"rb-datetime-clock-dial-container\"\n            [style.transform]=\"'rotate(' + (30 * i) + 'deg)'\">\n            <div \n                class=\"rb-datetime-clock-dial-number\"\n                [style.transform]=\"'rotate(-' + (30 * i) + 'deg)'\">\n            {{t}}\n            </div>\n        </div>  \n    </div>\n</div>\n"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/rb-popup-list/rb-popup-list.component.html":
/*!**************************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/rb-popup-list/rb-popup-list.component.html ***!
  \**************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<mat-list>\n    <mat-list-item\n        *ngFor=\"let item of hierarchy\">\n        <button \n            mat-button \n            class=\"rb-list-select-button\"\n            (click)=\"select(item)\">\n            {{item.data[config.displayattribute]}}\n        </button>\n        <button \n            mat-button \n            class=\"rb-list-expand-button\"\n            *ngIf=\"isHierarchical\"\n            (click)=\"colapse(item)\">\n                <mat-icon>expand_less</mat-icon>\n            </button>\n    </mat-list-item>    \n    <mat-list-item\n        *ngFor=\"let item of list\">\n        <button \n            mat-button \n            class=\"rb-list-select-button\"\n            (click)=\"select(item)\">\n            {{item.data[config.displayattribute]}}\n        </button>\n        <button \n            mat-button \n            class=\"rb-list-expand-button\"\n            *ngIf=\"isHierarchical\"\n            (click)=\"expand(item)\">\n                <mat-icon>expand_more</mat-icon>\n            </button>\n    </mat-list-item>\n</mat-list>\n<div\n    class=\"rb-spinner-container\"\n    *ngIf=\"isLoading\">\n    <mat-spinner\n        diameter=\"20\">\n    </mat-spinner>\n</div>"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/rb-related-input/rb-related-input.component.html":
/*!********************************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/rb-related-input/rb-related-input.component.html ***!
  \********************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<mat-form-field \n    [style.width]=\"(size * 15) + 'px'\" >\n    <mat-label>{{label}}</mat-label>\n    <input \n        #input\n        matInput \n        [(ngModel)]=\"displayvalue\"\n        (focus)=\"focus()\"\n        (blur)=\"blur()\"\n        (keydown)=\"keydown($event)\"\n        [readonly]=\"readonly\">\n    <mat-icon \n        matPrefix\n        class=\"rb-grey-icon\">{{icon}}</mat-icon>\n</mat-form-field>"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/rb-search/rb-search.component.html":
/*!******************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/rb-search/rb-search.component.html ***!
  \******************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<p>rb-search works!</p>\n"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/rb-textarea-input/rb-textarea-input.component.html":
/*!**********************************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/rb-textarea-input/rb-textarea-input.component.html ***!
  \**********************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<mat-form-field >\n        <mat-label>{{label}}</mat-label>\n        <textarea \n            #input\n            matInput \n            [(ngModel)]=\"displayvalue\"\n            (focus)=\"focus()\"\n            (blur)=\"blur()\"\n            (keydown)=\"keydown($event)\"\n            [readonly]=\"readonly\">\n        </textarea>\n        <mat-icon \n            matPrefix\n            class=\"rb-grey-icon\">{{icon}}</mat-icon>\n</mat-form-field>"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/rb-view-loader/rb-view-loader.component.html":
/*!****************************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/rb-view-loader/rb-view-loader.component.html ***!
  \****************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<div #container></div>"

/***/ }),

/***/ "./src/app/api.service.ts":
/*!********************************!*\
  !*** ./src/app/api.service.ts ***!
  \********************************/
/*! exports provided: ApiService */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "ApiService", function() { return ApiService; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");
/* harmony import */ var _angular_common_http__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common/http */ "./node_modules/@angular/common/fesm2015/http.js");
/* harmony import */ var ngx_cookie_service__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ngx-cookie-service */ "./node_modules/ngx-cookie-service/ngx-cookie-service.js");




const httpOptions = {
    headers: new _angular_common_http__WEBPACK_IMPORTED_MODULE_2__["HttpHeaders"]().set("Content-Type", "application/json"),
    withCredentials: true
};
let ApiService = class ApiService {
    constructor(http, cookieService) {
        this.http = http;
        this.cookieService = cookieService;
        //this.baseUrl = 'http://localhost';
    }
    listObjects(name, filter) {
        const req = {
            action: 'list',
            object: name,
            filter: filter,
            options: {
                addrelated: true,
                addvalidation: true
            }
        };
        return this.http.post(this.baseUrl + '/rbos', req, httpOptions);
    }
    listRelatedObjects(name, uid, attribute, filter, search) {
        const req = {
            action: 'list',
            object: name,
            uid: uid,
            attribute: attribute,
            filter: filter,
            search: search,
            options: {
                addrelated: true,
                addvalidation: true
            }
        };
        return this.http.post(this.baseUrl + '/rbos', req, httpOptions);
    }
    updateObject(name, uid, data) {
        const req = {
            action: 'update',
            object: name,
            uid: uid,
            data: data,
            options: {
                addrelated: true,
                addvalidation: true
            }
        };
        return this.http.post(this.baseUrl + '/rbos', req, httpOptions);
    }
};
ApiService.ctorParameters = () => [
    { type: _angular_common_http__WEBPACK_IMPORTED_MODULE_2__["HttpClient"] },
    { type: ngx_cookie_service__WEBPACK_IMPORTED_MODULE_3__["CookieService"] }
];
ApiService = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Injectable"])({
        providedIn: 'root'
    })
], ApiService);



/***/ }),

/***/ "./src/app/app.component.css":
/*!***********************************!*\
  !*** ./src/app/app.component.css ***!
  \***********************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = ":host {\r\n    display: flex;\r\n    flex: 1 1 auto;\r\n    flex-direction: column;\r\n    max-height: 100%;\r\n}\r\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbInNyYy9hcHAvYXBwLmNvbXBvbmVudC5jc3MiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7SUFDSSxhQUFhO0lBQ2IsY0FBYztJQUNkLHNCQUFzQjtJQUN0QixnQkFBZ0I7QUFDcEIiLCJmaWxlIjoic3JjL2FwcC9hcHAuY29tcG9uZW50LmNzcyIsInNvdXJjZXNDb250ZW50IjpbIjpob3N0IHtcclxuICAgIGRpc3BsYXk6IGZsZXg7XHJcbiAgICBmbGV4OiAxIDEgYXV0bztcclxuICAgIGZsZXgtZGlyZWN0aW9uOiBjb2x1bW47XHJcbiAgICBtYXgtaGVpZ2h0OiAxMDAlO1xyXG59Il19 */"

/***/ }),

/***/ "./src/app/app.component.ts":
/*!**********************************!*\
  !*** ./src/app/app.component.ts ***!
  \**********************************/
/*! exports provided: AppComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AppComponent", function() { return AppComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");
/* harmony import */ var _angular_material__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/material */ "./node_modules/@angular/material/esm2015/material.js");
/* harmony import */ var _angular_platform_browser__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/platform-browser */ "./node_modules/@angular/platform-browser/fesm2015/platform-browser.js");
/* harmony import */ var _api_service__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ./api.service */ "./src/app/api.service.ts");





let AppComponent = class AppComponent {
    constructor(elementRef, matIconRegistry, domSanitizer, apiService) {
        this.elementRef = elementRef;
        this.matIconRegistry = matIconRegistry;
        this.domSanitizer = domSanitizer;
        this.apiService = apiService;
        var native = this.elementRef.nativeElement;
        this.type = native.getAttribute("type");
        this.title = native.getAttribute("title");
        this.version = native.getAttribute("version");
        this.initialView = native.getAttribute("initialview");
        this.menuView = native.getAttribute("menuview");
        this.apiService.uiService = native.getAttribute("uiservice");
        this.apiService.objectService = native.getAttribute("objectservice");
        this.apiService.processService = native.getAttribute("processservice");
        let currentUrl = window.location.href;
        let pos = currentUrl.indexOf(this.apiService.uiService);
        this.apiService.baseUrl = currentUrl.substring(0, pos - 1);
        this.iconsets = native.getAttribute("iconsets").split(",");
        for (const set of this.iconsets) {
            this.matIconRegistry.addSvgIconSetInNamespace(set, this.domSanitizer.bypassSecurityTrustResourceUrl(this.apiService.baseUrl + '/' + this.apiService.uiService + '/resource/' + set + '.svg'));
        }
    }
};
AppComponent.ctorParameters = () => [
    { type: _angular_core__WEBPACK_IMPORTED_MODULE_1__["ElementRef"] },
    { type: _angular_material__WEBPACK_IMPORTED_MODULE_2__["MatIconRegistry"] },
    { type: _angular_platform_browser__WEBPACK_IMPORTED_MODULE_3__["DomSanitizer"] },
    { type: _api_service__WEBPACK_IMPORTED_MODULE_4__["ApiService"] }
];
AppComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
        viewProviders: [_angular_material__WEBPACK_IMPORTED_MODULE_2__["MatIconRegistry"]],
        selector: 'app-root',
        template: __webpack_require__(/*! raw-loader!./app.component.html */ "./node_modules/raw-loader/index.js!./src/app/app.component.html"),
        styles: [__webpack_require__(/*! ./app.component.css */ "./src/app/app.component.css")]
    })
], AppComponent);



/***/ }),

/***/ "./src/app/app.module.ts":
/*!*******************************!*\
  !*** ./src/app/app.module.ts ***!
  \*******************************/
/*! exports provided: AppModule */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AppModule", function() { return AppModule; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_platform_browser__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/platform-browser */ "./node_modules/@angular/platform-browser/fesm2015/platform-browser.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");
/* harmony import */ var _app_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./app.component */ "./src/app/app.component.ts");
/* harmony import */ var _redback_module__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ./redback.module */ "./src/app/redback.module.ts");
/* harmony import */ var _angular_material__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/material */ "./node_modules/@angular/material/esm2015/material.js");
/* harmony import */ var _angular_common_http__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @angular/common/http */ "./node_modules/@angular/common/fesm2015/http.js");







let AppModule = class AppModule {
};
AppModule = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_2__["NgModule"])({
        imports: [
            _angular_platform_browser__WEBPACK_IMPORTED_MODULE_1__["BrowserModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatIconModule"],
            _redback_module__WEBPACK_IMPORTED_MODULE_4__["RedbackModule"],
            _angular_common_http__WEBPACK_IMPORTED_MODULE_6__["HttpClientModule"]
        ],
        exports: [
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatIconModule"]
        ],
        declarations: [
            _app_component__WEBPACK_IMPORTED_MODULE_3__["AppComponent"]
        ],
        providers: [],
        bootstrap: [
            _app_component__WEBPACK_IMPORTED_MODULE_3__["AppComponent"]
        ]
    })
], AppModule);



/***/ }),

/***/ "./src/app/data.service.ts":
/*!*********************************!*\
  !*** ./src/app/data.service.ts ***!
  \*********************************/
/*! exports provided: DataService */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "DataService", function() { return DataService; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");
/* harmony import */ var _api_service__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./api.service */ "./src/app/api.service.ts");
/* harmony import */ var rxjs__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! rxjs */ "./node_modules/rxjs/_esm2015/index.js");
/* harmony import */ var _datamodel__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ./datamodel */ "./src/app/datamodel.ts");





let DataService = class DataService {
    constructor(apiService) {
        this.apiService = apiService;
        this.allObjects = [];
        this.saveImmediatly = true;
    }
    getLocalObject(objectname, uid) {
        let rbObject = null;
        for (const o of this.allObjects) {
            if (o.objectname == objectname && o.uid == uid) {
                rbObject = o;
            }
        }
        return rbObject;
    }
    listObjects(name, filter) {
        const listObs = this.apiService.listObjects(name, filter);
        const dataObservable = new rxjs__WEBPACK_IMPORTED_MODULE_3__["Observable"]((observer) => {
            listObs.subscribe(resp => {
                const rbObjectArray = Object.values(resp.list).map(json => this.updateObjectFromServer(json));
                observer.next(rbObjectArray);
                observer.complete();
            });
        });
        return dataObservable;
    }
    listRelatedObjects(name, uid, attribute, filter, search) {
        const listObs = this.apiService.listRelatedObjects(name, uid, attribute, filter, search);
        const dataObservable = new rxjs__WEBPACK_IMPORTED_MODULE_3__["Observable"]((observer) => {
            listObs.subscribe(resp => {
                const rbObjectArray = Object.values(resp.list).map(json => this.updateObjectFromServer(json));
                observer.next(rbObjectArray);
                observer.complete();
            });
        });
        return dataObservable;
    }
    updateObjectFromServer(json) {
        if (json.related != null) {
            for (const a in json.related) {
                const relatedJson = json.related[a];
                this.updateObjectFromServer(relatedJson);
            }
        }
        let rbObject = this.getLocalObject(json.objectname, json.uid);
        if (rbObject != null) {
            rbObject.updateFromServer(json);
        }
        else {
            rbObject = new _datamodel__WEBPACK_IMPORTED_MODULE_4__["RbObject"](json, this);
            this.allObjects.push(rbObject);
        }
        return rbObject;
    }
    updateObjectToServer(rbObject) {
        let upd = {};
        for (const attribute of rbObject.changed) {
            upd[attribute] = rbObject.data[attribute];
        }
        this.apiService.updateObject(rbObject.objectname, rbObject.uid, upd).subscribe(resp => this.updateObjectFromServer(resp));
    }
};
DataService.ctorParameters = () => [
    { type: _api_service__WEBPACK_IMPORTED_MODULE_2__["ApiService"] }
];
DataService = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Injectable"])({
        providedIn: 'root'
    })
], DataService);



/***/ }),

/***/ "./src/app/datamodel.ts":
/*!******************************!*\
  !*** ./src/app/datamodel.ts ***!
  \******************************/
/*! exports provided: ObjectResp, RbObject */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "ObjectResp", function() { return ObjectResp; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbObject", function() { return RbObject; });
/* harmony import */ var _data_service__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./data.service */ "./src/app/data.service.ts");

class ObjectResp {
}
class RbObject {
    constructor(json, ds) {
        this.uid = json.uid;
        this.objectname = json.objectname;
        this.domain = json.domain;
        this.data = json.data;
        this.related = json.related != null ? json.related : {};
        this.validation = json.validation != null ? json.validation : {};
        this.changed = [];
        this.dataService = ds;
        this.resolveRelatedObjects();
    }
    updateFromServer(json) {
        const inData = json.data;
        for (const attribute in json.data)
            this.data[attribute] = json.data[attribute];
        if (json.validation != null)
            for (const attribute in json.validation)
                this.validation[attribute] = json.validation[attribute];
        if (json.related != null)
            for (const attribute in json.related)
                this.related[attribute] = json.related[attribute];
        this.changed = [];
        this.resolveRelatedObjects();
    }
    resolveRelatedObjects() {
        for (const attribute in this.related) {
            this.related[attribute] = this.dataService.getLocalObject(this.related[attribute].objectname, this.related[attribute].uid);
        }
    }
    setValue(attribute, value) {
        this.setValueAndRelated(attribute, value, null);
    }
    setValueAndRelated(attribute, value, related) {
        if (this.validation[attribute].editable == true) {
            this.data[attribute] = value;
            this.changed.push(attribute);
            if (this.related[attribute] != null)
                this.related[attribute] = related;
            if (this.dataService.saveImmediatly)
                this.saveToServer();
        }
    }
    saveToServer() {
        this.dataService.updateObjectToServer(this);
    }
}
RbObject.ctorParameters = () => [
    { type: undefined },
    { type: _data_service__WEBPACK_IMPORTED_MODULE_0__["DataService"] }
];


/***/ }),

/***/ "./src/app/desktop-root/desktop-root.component.css":
/*!*********************************************************!*\
  !*** ./src/app/desktop-root/desktop-root.component.css ***!
  \*********************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = ":host {\r\n    display: flex;\r\n    flex: 1 1 auto;\r\n    flex-direction: column;\r\n    max-height: 100%;\r\n}\r\n\r\nmat-toolbar {\r\n    min-height:40px\r\n}\r\n\r\nmat-toolbar-row {\r\n    height: 40px;\r\n}\r\n\r\nmat-sidenav-container {\r\n    display: flex;\r\n    flex: 1 1 auto;\r\n}\r\n\r\nmat-sidenav {\r\n    width: 260px;\r\n    border-right: Solid 1px gray;\r\n}\r\n\r\nmat-sidenav-content {\r\n    display: flex;\r\n    flex: 1 1 auto;\r\n}\r\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbInNyYy9hcHAvZGVza3RvcC1yb290L2Rlc2t0b3Atcm9vdC5jb21wb25lbnQuY3NzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBO0lBQ0ksYUFBYTtJQUNiLGNBQWM7SUFDZCxzQkFBc0I7SUFDdEIsZ0JBQWdCO0FBQ3BCOztBQUVBO0lBQ0k7QUFDSjs7QUFFQTtJQUNJLFlBQVk7QUFDaEI7O0FBRUE7SUFDSSxhQUFhO0lBQ2IsY0FBYztBQUNsQjs7QUFFQTtJQUNJLFlBQVk7SUFDWiw0QkFBNEI7QUFDaEM7O0FBRUE7SUFDSSxhQUFhO0lBQ2IsY0FBYztBQUNsQiIsImZpbGUiOiJzcmMvYXBwL2Rlc2t0b3Atcm9vdC9kZXNrdG9wLXJvb3QuY29tcG9uZW50LmNzcyIsInNvdXJjZXNDb250ZW50IjpbIjpob3N0IHtcclxuICAgIGRpc3BsYXk6IGZsZXg7XHJcbiAgICBmbGV4OiAxIDEgYXV0bztcclxuICAgIGZsZXgtZGlyZWN0aW9uOiBjb2x1bW47XHJcbiAgICBtYXgtaGVpZ2h0OiAxMDAlO1xyXG59XHJcblxyXG5tYXQtdG9vbGJhciB7XHJcbiAgICBtaW4taGVpZ2h0OjQwcHhcclxufVxyXG5cclxubWF0LXRvb2xiYXItcm93IHtcclxuICAgIGhlaWdodDogNDBweDtcclxufVxyXG5cclxubWF0LXNpZGVuYXYtY29udGFpbmVyIHtcclxuICAgIGRpc3BsYXk6IGZsZXg7XHJcbiAgICBmbGV4OiAxIDEgYXV0bztcclxufVxyXG5cclxubWF0LXNpZGVuYXYge1xyXG4gICAgd2lkdGg6IDI2MHB4O1xyXG4gICAgYm9yZGVyLXJpZ2h0OiBTb2xpZCAxcHggZ3JheTtcclxufVxyXG5cclxubWF0LXNpZGVuYXYtY29udGVudCB7XHJcbiAgICBkaXNwbGF5OiBmbGV4O1xyXG4gICAgZmxleDogMSAxIGF1dG87XHJcbn0iXX0= */"

/***/ }),

/***/ "./src/app/desktop-root/desktop-root.component.ts":
/*!********************************************************!*\
  !*** ./src/app/desktop-root/desktop-root.component.ts ***!
  \********************************************************/
/*! exports provided: DesktopRootComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "DesktopRootComponent", function() { return DesktopRootComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");
/* harmony import */ var ngx_cookie_service__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ngx-cookie-service */ "./node_modules/ngx-cookie-service/ngx-cookie-service.js");
/* harmony import */ var app_api_service__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! app/api.service */ "./src/app/api.service.ts");




let DesktopRootComponent = class DesktopRootComponent {
    constructor(cookieService, apiService) {
        this.cookieService = cookieService;
        this.apiService = apiService;
    }
    ngOnInit() {
        this.cookieService.set('rbtoken', 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJlbWFpbCI6Im5ncm9uZGluNzhAZ21haWwuY29tIiwiZXhwIjoxOTIwNTExOTIyMDAwfQ.zQrN7sheh1PuO4fWru45dTPDtkLAqB9Q0WrwGO6yOeo', 1920511922000, "/", "http://localhost", false, 'Lax');
        if (this.version == null)
            this.version = 'default';
        this.view = this.initialView;
    }
    get viewUrl() {
        return this.apiService.baseUrl + '/' + this.apiService.uiService + '/view/' + this.version + '/' + this.view;
    }
    get menuUrl() {
        return this.apiService.baseUrl + '/' + this.apiService.uiService + '/menu/' + this.version + '/' + this.menuView;
    }
    navigateTo($event) {
        this.view = $event.view;
    }
};
DesktopRootComponent.ctorParameters = () => [
    { type: ngx_cookie_service__WEBPACK_IMPORTED_MODULE_2__["CookieService"] },
    { type: app_api_service__WEBPACK_IMPORTED_MODULE_3__["ApiService"] }
];
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])()
], DesktopRootComponent.prototype, "title", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])()
], DesktopRootComponent.prototype, "initialView", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])()
], DesktopRootComponent.prototype, "menuView", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])()
], DesktopRootComponent.prototype, "version", void 0);
DesktopRootComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
        selector: 'desktop-root',
        template: __webpack_require__(/*! raw-loader!./desktop-root.component.html */ "./node_modules/raw-loader/index.js!./src/app/desktop-root/desktop-root.component.html"),
        styles: [__webpack_require__(/*! ./desktop-root.component.css */ "./src/app/desktop-root/desktop-root.component.css")]
    })
], DesktopRootComponent);



/***/ }),

/***/ "./src/app/rb-dataset/rb-dataset.directive.ts":
/*!****************************************************!*\
  !*** ./src/app/rb-dataset/rb-dataset.directive.ts ***!
  \****************************************************/
/*! exports provided: RbDatasetDirective */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbDatasetDirective", function() { return RbDatasetDirective; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");
/* harmony import */ var _data_service__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../data.service */ "./src/app/data.service.ts");



let RbDatasetDirective = class RbDatasetDirective {
    constructor(dataService) {
        this.dataService = dataService;
        this.list = [];
    }
    ngOnInit() {
        this.getData();
    }
    ngOnChanges(changes) {
        if (this.active)
            this.getData();
        else
            this.list = [];
    }
    getData() {
        this.list = [];
        this.selectedObject = null;
        let filter = null;
        if (this.baseFilter != null)
            filter = this.baseFilter;
        else
            filter = {};
        if (this.relatedFilter != null) {
            if (this.relatedObject != null) {
                for (const key in this.relatedFilter) {
                    let value = this.relatedFilter[key];
                    if (typeof value == "string" && value.startsWith("[") && value.endsWith("]")) {
                        let attr = value.substring(1, value.length - 1);
                        if (attr == 'uid')
                            value = this.relatedObject.uid;
                        else
                            value = this.relatedObject.data[attr];
                    }
                    filter[key] = value;
                }
            }
            else {
                filter = null;
            }
        }
        if (filter != null) {
            this.dataService.listObjects(this.objectname, filter).subscribe(data => this.setData(data));
            this.isLoading = true;
        }
    }
    setData(data) {
        this.list = data;
        this.isLoading = false;
    }
    select(item) {
        this.selectedObject = item;
    }
};
RbDatasetDirective.ctorParameters = () => [
    { type: _data_service__WEBPACK_IMPORTED_MODULE_2__["DataService"] }
];
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('object')
], RbDatasetDirective.prototype, "objectname", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('relatedObject')
], RbDatasetDirective.prototype, "relatedObject", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('relatedFilter')
], RbDatasetDirective.prototype, "relatedFilter", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('baseFilter')
], RbDatasetDirective.prototype, "baseFilter", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('active')
], RbDatasetDirective.prototype, "active", void 0);
RbDatasetDirective = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Directive"])({
        selector: 'rb-dataset',
        exportAs: 'dataset'
    })
], RbDatasetDirective);



/***/ }),

/***/ "./src/app/rb-datetime-input/rb-datetime-input.component.css":
/*!*******************************************************************!*\
  !*** ./src/app/rb-datetime-input/rb-datetime-input.component.css ***!
  \*******************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL3JiLWRhdGV0aW1lLWlucHV0L3JiLWRhdGV0aW1lLWlucHV0LmNvbXBvbmVudC5jc3MifQ== */"

/***/ }),

/***/ "./src/app/rb-datetime-input/rb-datetime-input.component.ts":
/*!******************************************************************!*\
  !*** ./src/app/rb-datetime-input/rb-datetime-input.component.ts ***!
  \******************************************************************/
/*! exports provided: RbDatetimeInputComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbDatetimeInputComponent", function() { return RbDatetimeInputComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");
/* harmony import */ var _angular_cdk_overlay__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/cdk/overlay */ "./node_modules/@angular/cdk/esm2015/overlay.js");
/* harmony import */ var app_tokens__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! app/tokens */ "./src/app/tokens.ts");
/* harmony import */ var _angular_cdk_portal__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/cdk/portal */ "./node_modules/@angular/cdk/esm2015/portal.js");
/* harmony import */ var app_rb_popup_datetime_rb_popup_datetime_component__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! app/rb-popup-datetime/rb-popup-datetime.component */ "./src/app/rb-popup-datetime/rb-popup-datetime.component.ts");






let RbDatetimeInputComponent = class RbDatetimeInputComponent {
    constructor(injector, overlay, viewContainerRef) {
        this.injector = injector;
        this.overlay = overlay;
        this.viewContainerRef = viewContainerRef;
    }
    ngOnInit() {
        if (this.format == null)
            this.format = 'YYYY-MM-DD HH:mm';
    }
    get displayvalue() {
        if (this.rbObject != null) {
            let iso = this.rbObject.data[this.attribute];
            if (iso != null) {
                let dt = new Date(iso);
                let val = this.format;
                val = val.replace('YYYY', dt.getFullYear().toString());
                val = val.replace('YY', (dt.getFullYear() % 100).toString());
                val = val.replace('MM', this.convertToStringAndPad(dt.getMonth() + 1, 2));
                val = val.replace('DD', this.convertToStringAndPad(dt.getDate(), 2));
                val = val.replace('HH', this.convertToStringAndPad(dt.getHours(), 2));
                val = val.replace('mm', this.convertToStringAndPad(dt.getMinutes(), 2));
                return val;
            }
            else {
                return null;
            }
        }
        else
            return null;
    }
    convertToStringAndPad(num, n) {
        let ret = num.toString();
        while (ret.length < n)
            ret = '0' + ret;
        return ret;
    }
    get readonly() {
        if (this.rbObject != null && this.rbObject.validation[this.attribute] != null)
            return !(this.editable && this.rbObject.validation[this.attribute].editable);
        else
            return true;
    }
    openPopupList() {
        if (!this.readonly) {
            this.overlayRef = this.overlay.create({
                positionStrategy: this.overlay.position().connectedTo(this.inputContainerRef.element, { originX: 'start', originY: 'bottom' }, { overlayX: 'start', overlayY: 'top' }),
                hasBackdrop: true,
                backdropClass: 'cdk-overlay-transparent-backdrop'
            });
            this.overlayRef.backdropClick().subscribe(() => {
                this.cancelEditing();
            });
            let config = new app_rb_popup_datetime_rb_popup_datetime_component__WEBPACK_IMPORTED_MODULE_5__["DateTimePopupConfig"]();
            config.initialDate = this.rbObject != null && this.rbObject.data[this.attribute] != null ? new Date(this.rbObject.data[this.attribute]) : new Date();
            config.datePart = true;
            config.hourPart = true;
            config.minutePart = true;
            const injectorTokens = new WeakMap();
            injectorTokens.set(_angular_cdk_overlay__WEBPACK_IMPORTED_MODULE_2__["OverlayRef"], this.overlayRef);
            injectorTokens.set(app_tokens__WEBPACK_IMPORTED_MODULE_3__["CONTAINER_DATA"], config);
            let inj = new _angular_cdk_portal__WEBPACK_IMPORTED_MODULE_4__["PortalInjector"](this.injector, injectorTokens);
            const popupListPortal = new _angular_cdk_portal__WEBPACK_IMPORTED_MODULE_4__["ComponentPortal"](app_rb_popup_datetime_rb_popup_datetime_component__WEBPACK_IMPORTED_MODULE_5__["RbPopupDatetimeComponent"], this.viewContainerRef, inj);
            this.popupDatetimeComponentRef = this.overlayRef.attach(popupListPortal);
            this.popupDatetimeComponentRef.instance.selected.subscribe(object => this.selected(object));
        }
    }
    focus(event) {
        if (this.overlayRef == null)
            this.openPopupList();
    }
    blur(event) {
        if (this.overlayRef != null)
            this.inputContainerRef.element.nativeElement.focus();
    }
    keydown(event) {
        if (event.keyCode == 13) {
            this.selected(null);
        }
        else if (event.keyCode == 9 || event.keyCode == 27) {
            this.cancelEditing();
        }
    }
    cancelEditing() {
        this.overlayRef.dispose();
        this.overlayRef = null;
        this.inputContainerRef.element.nativeElement.blur();
    }
    selected(dt) {
        if (dt != null)
            this.rbObject.setValue(this.attribute, dt.toISOString());
        else
            this.rbObject.setValue(this.attribute, null);
        this.overlayRef.dispose();
        this.overlayRef = null;
        this.inputContainerRef.element.nativeElement.blur();
    }
};
RbDatetimeInputComponent.ctorParameters = () => [
    { type: _angular_core__WEBPACK_IMPORTED_MODULE_1__["Injector"] },
    { type: _angular_cdk_overlay__WEBPACK_IMPORTED_MODULE_2__["Overlay"] },
    { type: _angular_core__WEBPACK_IMPORTED_MODULE_1__["ViewContainerRef"] }
];
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('label')
], RbDatetimeInputComponent.prototype, "label", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('icon')
], RbDatetimeInputComponent.prototype, "icon", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('size')
], RbDatetimeInputComponent.prototype, "size", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('editable')
], RbDatetimeInputComponent.prototype, "editable", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('object')
], RbDatetimeInputComponent.prototype, "rbObject", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('attribute')
], RbDatetimeInputComponent.prototype, "attribute", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('format')
], RbDatetimeInputComponent.prototype, "format", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["ViewChild"])('input', { read: _angular_core__WEBPACK_IMPORTED_MODULE_1__["ViewContainerRef"], static: false })
], RbDatetimeInputComponent.prototype, "inputContainerRef", void 0);
RbDatetimeInputComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
        selector: 'rb-datetime-input',
        template: __webpack_require__(/*! raw-loader!./rb-datetime-input.component.html */ "./node_modules/raw-loader/index.js!./src/app/rb-datetime-input/rb-datetime-input.component.html"),
        styles: [__webpack_require__(/*! ./rb-datetime-input.component.css */ "./src/app/rb-datetime-input/rb-datetime-input.component.css")]
    })
], RbDatetimeInputComponent);



/***/ }),

/***/ "./src/app/rb-duration-input/rb-duration-input.component.css":
/*!*******************************************************************!*\
  !*** ./src/app/rb-duration-input/rb-duration-input.component.css ***!
  \*******************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL3JiLWR1cmF0aW9uLWlucHV0L3JiLWR1cmF0aW9uLWlucHV0LmNvbXBvbmVudC5jc3MifQ== */"

/***/ }),

/***/ "./src/app/rb-duration-input/rb-duration-input.component.ts":
/*!******************************************************************!*\
  !*** ./src/app/rb-duration-input/rb-duration-input.component.ts ***!
  \******************************************************************/
/*! exports provided: RbDurationInputComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbDurationInputComponent", function() { return RbDurationInputComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");


let RbDurationInputComponent = class RbDurationInputComponent {
    constructor() { }
    ngOnInit() {
        this.editing = false;
    }
    get displayvalue() {
        if (this.editing) {
            return this.editingValue;
        }
        else {
            if (this.rbObject != null && this.rbObject.data[this.attribute] != null) {
                let ms = this.rbObject.data[this.attribute];
                let years = Math.floor(ms / 31536000000);
                let weeks = Math.floor((ms % 31536000000) / 604800000);
                let days = Math.floor((ms % 604800000) / 86400000);
                let hours = Math.floor((ms % 86400000) / 3600000);
                let minutes = Math.floor((ms % 3600000) / 60000);
                let seconds = Math.floor((ms % 60000) / 1000);
                let milli = Math.floor((ms % 1000));
                let val = "";
                if (years != 0)
                    val = val + " " + years + "y";
                if (weeks != 0)
                    val = val + " " + weeks + "w";
                if (days != 0)
                    val = val + " " + days + "d";
                if (hours != 0)
                    val = val + " " + hours + "h";
                if (minutes != 0)
                    val = val + " " + minutes + "m";
                if (seconds != 0)
                    val = val + " " + seconds + "s";
                if (milli != 0)
                    val = val + " " + milli + "ms";
                return val.substr(1);
            }
            else {
                return null;
            }
        }
    }
    set displayvalue(val) {
        this.editingValue = val;
    }
    get readonly() {
        if (this.rbObject != null && this.rbObject.validation[this.attribute] != null)
            return !(this.editable && this.rbObject.validation[this.attribute].editable);
        else
            return true;
    }
    keydown(event) {
    }
    focus(event) {
    }
    blur(event) {
    }
    commit() {
        let val = 0;
        let multiplier = -1;
        let str = "";
        let strParts = this.editingValue.split('');
        for (let c of strParts) {
            if (c == 'y' || c == 'Y')
                multiplier = 31536000000;
            else if (c == 'w' || c == 'W')
                multiplier = 604800000;
            else if (c == 'd' || c == 'D')
                multiplier = 86400000;
            else if (c == 'h' || c == 'H')
                multiplier = 3600000;
            else if (c == 'm' || c == 'M')
                multiplier = 60000;
            else if (c == 's' || c == 'S')
                multiplier = 1000;
            else
                str = str + c;
            if (multiplier > -1) {
                val += Number.parseFloat(str) * multiplier;
                str = "";
                multiplier = -1;
            }
        }
        if (str.length > 0)
            val = val + Number.parseInt(str);
        this.rbObject.setValue(this.attribute, val);
        this.editing = false;
        this.editingValue = null;
    }
};
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('label')
], RbDurationInputComponent.prototype, "label", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('icon')
], RbDurationInputComponent.prototype, "icon", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('size')
], RbDurationInputComponent.prototype, "size", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('editable')
], RbDurationInputComponent.prototype, "editable", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('object')
], RbDurationInputComponent.prototype, "rbObject", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('attribute')
], RbDurationInputComponent.prototype, "attribute", void 0);
RbDurationInputComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
        selector: 'rb-duration-input',
        template: __webpack_require__(/*! raw-loader!./rb-duration-input.component.html */ "./node_modules/raw-loader/index.js!./src/app/rb-duration-input/rb-duration-input.component.html"),
        styles: [__webpack_require__(/*! ./rb-duration-input.component.css */ "./src/app/rb-duration-input/rb-duration-input.component.css")]
    })
], RbDurationInputComponent);



/***/ }),

/***/ "./src/app/rb-input/rb-input.component.css":
/*!*************************************************!*\
  !*** ./src/app/rb-input/rb-input.component.css ***!
  \*************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL3JiLWlucHV0L3JiLWlucHV0LmNvbXBvbmVudC5jc3MifQ== */"

/***/ }),

/***/ "./src/app/rb-input/rb-input.component.ts":
/*!************************************************!*\
  !*** ./src/app/rb-input/rb-input.component.ts ***!
  \************************************************/
/*! exports provided: RbInputComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbInputComponent", function() { return RbInputComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");


let RbInputComponent = class RbInputComponent {
    constructor() { }
    ngOnInit() {
    }
    get value() {
        if (this.rbObject != null)
            return this.rbObject.data[this.attribute];
        else
            return null;
    }
    set value(val) {
        this.editedValue = val;
    }
    get readonly() {
        if (this.rbObject != null && this.rbObject.validation[this.attribute] != null)
            return !(this.editable && this.rbObject.validation[this.attribute].editable);
        else
            return true;
    }
    commit() {
        this.rbObject.setValue(this.attribute, this.editedValue);
    }
};
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('label')
], RbInputComponent.prototype, "label", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('icon')
], RbInputComponent.prototype, "icon", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('size')
], RbInputComponent.prototype, "size", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('editable')
], RbInputComponent.prototype, "editable", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('object')
], RbInputComponent.prototype, "rbObject", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('attribute')
], RbInputComponent.prototype, "attribute", void 0);
RbInputComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
        selector: 'rb-input',
        template: __webpack_require__(/*! raw-loader!./rb-input.component.html */ "./node_modules/raw-loader/index.js!./src/app/rb-input/rb-input.component.html"),
        styles: [__webpack_require__(/*! ./rb-input.component.css */ "./src/app/rb-input/rb-input.component.css")]
    })
], RbInputComponent);



/***/ }),

/***/ "./src/app/rb-list-scroll/rb-list-scroll.directive.ts":
/*!************************************************************!*\
  !*** ./src/app/rb-list-scroll/rb-list-scroll.directive.ts ***!
  \************************************************************/
/*! exports provided: RbListScrollDirective */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbListScrollDirective", function() { return RbListScrollDirective; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");


let RbListScrollDirective = class RbListScrollDirective {
    constructor() {
        this.list = 'allo';
        this.list = 'all1';
    }
};
RbListScrollDirective = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Directive"])({
        selector: 'rb-list-scroll'
    })
], RbListScrollDirective);



/***/ }),

/***/ "./src/app/rb-map/rb-map.component.css":
/*!*********************************************!*\
  !*** ./src/app/rb-map/rb-map.component.css ***!
  \*********************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL3JiLW1hcC9yYi1tYXAuY29tcG9uZW50LmNzcyJ9 */"

/***/ }),

/***/ "./src/app/rb-map/rb-map.component.ts":
/*!********************************************!*\
  !*** ./src/app/rb-map/rb-map.component.ts ***!
  \********************************************/
/*! exports provided: RbMapComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbMapComponent", function() { return RbMapComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");


let RbMapComponent = class RbMapComponent {
    constructor() { }
    ngOnInit() {
    }
};
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('object')
], RbMapComponent.prototype, "object", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('list')
], RbMapComponent.prototype, "list", void 0);
RbMapComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
        selector: 'rb-map',
        template: __webpack_require__(/*! raw-loader!./rb-map.component.html */ "./node_modules/raw-loader/index.js!./src/app/rb-map/rb-map.component.html"),
        exportAs: 'rbMap',
        styles: [__webpack_require__(/*! ./rb-map.component.css */ "./src/app/rb-map/rb-map.component.css")]
    })
], RbMapComponent);



/***/ }),

/***/ "./src/app/rb-menu/rb-menu.directive.ts":
/*!**********************************************!*\
  !*** ./src/app/rb-menu/rb-menu.directive.ts ***!
  \**********************************************/
/*! exports provided: RbMenuDirective */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbMenuDirective", function() { return RbMenuDirective; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");


let RbMenuDirective = class RbMenuDirective {
    constructor() {
        this.largemenu = true;
        this.groupOpen = [];
    }
    get isLarge() {
        return true;
    }
    toggleGroup(grp) {
        if (this.groupOpen[grp] == null)
            this.groupOpen[grp] = true;
        else
            this.groupOpen[grp] = !this.groupOpen[grp];
    }
    isGroupOpen(grp) {
        if (this.groupOpen[grp] != null)
            return this.groupOpen[grp];
        else
            return false;
    }
};
RbMenuDirective = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Directive"])({
        selector: 'rb-menu',
        exportAs: 'menu',
    })
], RbMenuDirective);



/***/ }),

/***/ "./src/app/rb-popup-datetime/rb-popup-datetime.component.css":
/*!*******************************************************************!*\
  !*** ./src/app/rb-popup-datetime/rb-popup-datetime.component.css ***!
  \*******************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = ":host {\n    background: white;\n    border-radius: 4px;\n    box-shadow: 0 7px 8px -4px rgba(0, 0, 0, 0.2),\n        0 13px 19px 2px rgba(0, 0, 0, 0.14),\n        0 5px 24px 4px rgba(0, 0, 0, 0.12);\n}\n\n.rb-datetime-cal-header {\n    display:flex;\n    flex-direction: row;\n    line-height: 36px;\n}\n\n.rb-datetime-cal-title {\n    display:flex;\n    flex: 1 0 auto;\n    text-align: center;\n    justify-content: center;\n}\n\n.rb-datetime-cal-monthnav {\n    display:flex;\n    flex: 0 0 auto;\n    text-align: center;\n    justify-content: center;\n    width: 32px;\n    cursor: pointer\n}\n\n.rb-datetime-cal-daybox {\n    display:flex;\n    width:32px;\n    line-height:32px;\n    display: flex;\n    font-size: 13px;\n    color: #666666;\n    justify-content: center;\n    cursor: pointer;\n    border-radius: 50%;\n}\n\n.rb-datetime-cal-dayheader {\n    display:flex;\n    width:32px;\n    line-height:32px;\n    display: flex;\n    font-size: 13px;\n    color: #666666;\n    justify-content: center;\n}\n\n.rb-datetime-cal-emptydaybox {\n    display:flex;\n    width:32px;\n    line-height:32px;\n    display: flex;\n}\n\n.rb-datetime-cal-weekline {\n    height:32px;\n    display: flex;\n}\n\n.rb-datetime-clock-back {\n    background-color:#eeeeee;\n    border-radius: 50%;\n    height: 200px;\n    position: relative;\n    width: 200px;\n    margin: 10px;\n    cursor: pointer;\n}\n\n.rb-datetime-clock-dial-container {\n    position: absolute;\n    top: 0;\n    right: 0;\n    bottom: 0;\n    left: 0;\n}\n\n.rb-datetime-clock-dial-pointer {\n    background: darkblue;\n    height: 35%;\n    left: 49%;\n    position: absolute;\n    top: 15%;\n    transform-origin: 50% 100%;\n    width: 1%;\n}\n\n.rb-datetime-clock-dial-number {\n    background: transparent;\n    color: #666666;\n    position: absolute;\n    top: 5%;\n    left: 45%;\n    width: 10%;\n    height: 10%;\n    transform-origin: 50% 50%;\n    cursor: pointer;\n    text-align: center;\n    font-size: 13px;\n}\n  \n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbInNyYy9hcHAvcmItcG9wdXAtZGF0ZXRpbWUvcmItcG9wdXAtZGF0ZXRpbWUuY29tcG9uZW50LmNzcyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiQUFBQTtJQUNJLGlCQUFpQjtJQUNqQixrQkFBa0I7SUFDbEI7OzBDQUVzQztBQUMxQzs7QUFFQTtJQUNJLFlBQVk7SUFDWixtQkFBbUI7SUFDbkIsaUJBQWlCO0FBQ3JCOztBQUVBO0lBQ0ksWUFBWTtJQUNaLGNBQWM7SUFDZCxrQkFBa0I7SUFDbEIsdUJBQXVCO0FBQzNCOztBQUVBO0lBQ0ksWUFBWTtJQUNaLGNBQWM7SUFDZCxrQkFBa0I7SUFDbEIsdUJBQXVCO0lBQ3ZCLFdBQVc7SUFDWDtBQUNKOztBQUVBO0lBQ0ksWUFBWTtJQUNaLFVBQVU7SUFDVixnQkFBZ0I7SUFDaEIsYUFBYTtJQUNiLGVBQWU7SUFDZixjQUFjO0lBQ2QsdUJBQXVCO0lBQ3ZCLGVBQWU7SUFDZixrQkFBa0I7QUFDdEI7O0FBRUE7SUFDSSxZQUFZO0lBQ1osVUFBVTtJQUNWLGdCQUFnQjtJQUNoQixhQUFhO0lBQ2IsZUFBZTtJQUNmLGNBQWM7SUFDZCx1QkFBdUI7QUFDM0I7O0FBRUE7SUFDSSxZQUFZO0lBQ1osVUFBVTtJQUNWLGdCQUFnQjtJQUNoQixhQUFhO0FBQ2pCOztBQUVBO0lBQ0ksV0FBVztJQUNYLGFBQWE7QUFDakI7O0FBRUE7SUFDSSx3QkFBd0I7SUFDeEIsa0JBQWtCO0lBQ2xCLGFBQWE7SUFDYixrQkFBa0I7SUFDbEIsWUFBWTtJQUNaLFlBQVk7SUFDWixlQUFlO0FBQ25COztBQUVBO0lBQ0ksa0JBQWtCO0lBQ2xCLE1BQU07SUFDTixRQUFRO0lBQ1IsU0FBUztJQUNULE9BQU87QUFDWDs7QUFFQTtJQUNJLG9CQUFvQjtJQUNwQixXQUFXO0lBQ1gsU0FBUztJQUNULGtCQUFrQjtJQUNsQixRQUFRO0lBQ1IsMEJBQTBCO0lBQzFCLFNBQVM7QUFDYjs7QUFFQTtJQUNJLHVCQUF1QjtJQUN2QixjQUFjO0lBQ2Qsa0JBQWtCO0lBQ2xCLE9BQU87SUFDUCxTQUFTO0lBQ1QsVUFBVTtJQUNWLFdBQVc7SUFDWCx5QkFBeUI7SUFDekIsZUFBZTtJQUNmLGtCQUFrQjtJQUNsQixlQUFlO0FBQ25CIiwiZmlsZSI6InNyYy9hcHAvcmItcG9wdXAtZGF0ZXRpbWUvcmItcG9wdXAtZGF0ZXRpbWUuY29tcG9uZW50LmNzcyIsInNvdXJjZXNDb250ZW50IjpbIjpob3N0IHtcbiAgICBiYWNrZ3JvdW5kOiB3aGl0ZTtcbiAgICBib3JkZXItcmFkaXVzOiA0cHg7XG4gICAgYm94LXNoYWRvdzogMCA3cHggOHB4IC00cHggcmdiYSgwLCAwLCAwLCAwLjIpLFxuICAgICAgICAwIDEzcHggMTlweCAycHggcmdiYSgwLCAwLCAwLCAwLjE0KSxcbiAgICAgICAgMCA1cHggMjRweCA0cHggcmdiYSgwLCAwLCAwLCAwLjEyKTtcbn1cblxuLnJiLWRhdGV0aW1lLWNhbC1oZWFkZXIge1xuICAgIGRpc3BsYXk6ZmxleDtcbiAgICBmbGV4LWRpcmVjdGlvbjogcm93O1xuICAgIGxpbmUtaGVpZ2h0OiAzNnB4O1xufVxuXG4ucmItZGF0ZXRpbWUtY2FsLXRpdGxlIHtcbiAgICBkaXNwbGF5OmZsZXg7XG4gICAgZmxleDogMSAwIGF1dG87XG4gICAgdGV4dC1hbGlnbjogY2VudGVyO1xuICAgIGp1c3RpZnktY29udGVudDogY2VudGVyO1xufVxuXG4ucmItZGF0ZXRpbWUtY2FsLW1vbnRobmF2IHtcbiAgICBkaXNwbGF5OmZsZXg7XG4gICAgZmxleDogMCAwIGF1dG87XG4gICAgdGV4dC1hbGlnbjogY2VudGVyO1xuICAgIGp1c3RpZnktY29udGVudDogY2VudGVyO1xuICAgIHdpZHRoOiAzMnB4O1xuICAgIGN1cnNvcjogcG9pbnRlclxufVxuXG4ucmItZGF0ZXRpbWUtY2FsLWRheWJveCB7XG4gICAgZGlzcGxheTpmbGV4O1xuICAgIHdpZHRoOjMycHg7XG4gICAgbGluZS1oZWlnaHQ6MzJweDtcbiAgICBkaXNwbGF5OiBmbGV4O1xuICAgIGZvbnQtc2l6ZTogMTNweDtcbiAgICBjb2xvcjogIzY2NjY2NjtcbiAgICBqdXN0aWZ5LWNvbnRlbnQ6IGNlbnRlcjtcbiAgICBjdXJzb3I6IHBvaW50ZXI7XG4gICAgYm9yZGVyLXJhZGl1czogNTAlO1xufVxuXG4ucmItZGF0ZXRpbWUtY2FsLWRheWhlYWRlciB7XG4gICAgZGlzcGxheTpmbGV4O1xuICAgIHdpZHRoOjMycHg7XG4gICAgbGluZS1oZWlnaHQ6MzJweDtcbiAgICBkaXNwbGF5OiBmbGV4O1xuICAgIGZvbnQtc2l6ZTogMTNweDtcbiAgICBjb2xvcjogIzY2NjY2NjtcbiAgICBqdXN0aWZ5LWNvbnRlbnQ6IGNlbnRlcjtcbn1cblxuLnJiLWRhdGV0aW1lLWNhbC1lbXB0eWRheWJveCB7XG4gICAgZGlzcGxheTpmbGV4O1xuICAgIHdpZHRoOjMycHg7XG4gICAgbGluZS1oZWlnaHQ6MzJweDtcbiAgICBkaXNwbGF5OiBmbGV4O1xufVxuXG4ucmItZGF0ZXRpbWUtY2FsLXdlZWtsaW5lIHtcbiAgICBoZWlnaHQ6MzJweDtcbiAgICBkaXNwbGF5OiBmbGV4O1xufVxuXG4ucmItZGF0ZXRpbWUtY2xvY2stYmFjayB7XG4gICAgYmFja2dyb3VuZC1jb2xvcjojZWVlZWVlO1xuICAgIGJvcmRlci1yYWRpdXM6IDUwJTtcbiAgICBoZWlnaHQ6IDIwMHB4O1xuICAgIHBvc2l0aW9uOiByZWxhdGl2ZTtcbiAgICB3aWR0aDogMjAwcHg7XG4gICAgbWFyZ2luOiAxMHB4O1xuICAgIGN1cnNvcjogcG9pbnRlcjtcbn1cblxuLnJiLWRhdGV0aW1lLWNsb2NrLWRpYWwtY29udGFpbmVyIHtcbiAgICBwb3NpdGlvbjogYWJzb2x1dGU7XG4gICAgdG9wOiAwO1xuICAgIHJpZ2h0OiAwO1xuICAgIGJvdHRvbTogMDtcbiAgICBsZWZ0OiAwO1xufVxuXG4ucmItZGF0ZXRpbWUtY2xvY2stZGlhbC1wb2ludGVyIHtcbiAgICBiYWNrZ3JvdW5kOiBkYXJrYmx1ZTtcbiAgICBoZWlnaHQ6IDM1JTtcbiAgICBsZWZ0OiA0OSU7XG4gICAgcG9zaXRpb246IGFic29sdXRlO1xuICAgIHRvcDogMTUlO1xuICAgIHRyYW5zZm9ybS1vcmlnaW46IDUwJSAxMDAlO1xuICAgIHdpZHRoOiAxJTtcbn1cblxuLnJiLWRhdGV0aW1lLWNsb2NrLWRpYWwtbnVtYmVyIHtcbiAgICBiYWNrZ3JvdW5kOiB0cmFuc3BhcmVudDtcbiAgICBjb2xvcjogIzY2NjY2NjtcbiAgICBwb3NpdGlvbjogYWJzb2x1dGU7XG4gICAgdG9wOiA1JTtcbiAgICBsZWZ0OiA0NSU7XG4gICAgd2lkdGg6IDEwJTtcbiAgICBoZWlnaHQ6IDEwJTtcbiAgICB0cmFuc2Zvcm0tb3JpZ2luOiA1MCUgNTAlO1xuICAgIGN1cnNvcjogcG9pbnRlcjtcbiAgICB0ZXh0LWFsaWduOiBjZW50ZXI7XG4gICAgZm9udC1zaXplOiAxM3B4O1xufVxuICAiXX0= */"

/***/ }),

/***/ "./src/app/rb-popup-datetime/rb-popup-datetime.component.ts":
/*!******************************************************************!*\
  !*** ./src/app/rb-popup-datetime/rb-popup-datetime.component.ts ***!
  \******************************************************************/
/*! exports provided: DateTimePopupConfig, RbPopupDatetimeComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "DateTimePopupConfig", function() { return DateTimePopupConfig; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbPopupDatetimeComponent", function() { return RbPopupDatetimeComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");
/* harmony import */ var app_tokens__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! app/tokens */ "./src/app/tokens.ts");
/* harmony import */ var _angular_cdk_overlay__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/cdk/overlay */ "./node_modules/@angular/cdk/esm2015/overlay.js");
/* harmony import */ var app_data_service__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! app/data.service */ "./src/app/data.service.ts");





class DateTimePopupConfig {
}
let RbPopupDatetimeComponent = class RbPopupDatetimeComponent {
    constructor(config, overlayRef, dataService) {
        this.config = config;
        this.overlayRef = overlayRef;
        this.dataService = dataService;
        this.selected = new _angular_core__WEBPACK_IMPORTED_MODULE_1__["EventEmitter"]();
        this.currentPart = 0;
        this.daysOfWeek = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];
        this.monthsOfYear = ['January', 'Feburary', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
        this.hoursOfDay = [0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22];
        this.minutesOfHour = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55];
    }
    ngOnInit() {
        this.year = this.config.initialDate.getFullYear();
        this.month = this.config.initialDate.getMonth();
        this.day = this.config.initialDate.getDate();
        this.hour = this.config.initialDate.getHours();
        this.minute = this.config.initialDate.getMinutes();
        this.calcCalendarSettings();
    }
    getDate() {
        let newDate = new Date();
        newDate.setFullYear(this.year);
        newDate.setMonth(this.month);
        newDate.setDate(this.day);
        newDate.setHours(this.hour);
        newDate.setMinutes(this.minute);
        newDate.setSeconds(0);
        newDate.setMilliseconds(0);
        return newDate;
    }
    calcCalendarSettings() {
        let firstOfTheMonth = new Date(this.getDate().setDate(1));
        let firstOfNextMonth = new Date((new Date(firstOfTheMonth.getTime() + 2678400000)).setDate(1));
        let daysInMonth = (firstOfNextMonth.getTime() - firstOfTheMonth.getTime()) / 86400000;
        this.firstDayOfMonth = firstOfTheMonth.getDay() - 1;
        this.numberOfWeeks = (daysInMonth + this.firstDayOfMonth + 1) / 7;
        let day = -this.firstDayOfMonth;
        this.calendar = [];
        for (let w = 0; w < this.numberOfWeeks; w++) {
            let week = [];
            for (let d = 0; d < 7; d++) {
                if (day < 1 || day > daysInMonth)
                    week.push("");
                else
                    week.push(day);
                day++;
            }
            this.calendar.push(week);
        }
    }
    nextMonth() {
        this.month++;
        if (this.month > 11) {
            this.year++;
            this.month = 0;
        }
        this.calcCalendarSettings();
    }
    previousMonth() {
        this.month--;
        if (this.month < 0) {
            this.year--;
            this.month = 11;
        }
        this.calcCalendarSettings();
    }
    selectDate(dayOfMonth) {
        this.day = dayOfMonth;
        this.nextPart();
    }
    selectHour(event) {
        this.hour = Math.round((this.getAngleFromClick(event) + 7.5) / 15);
        this.nextPart();
    }
    selectMinute(event) {
        this.minute = Math.round((this.getAngleFromClick(event) + 3) / 6);
        this.nextPart();
    }
    nextPart() {
        this.currentPart++;
        if (this.currentPart == 1 && this.config.hourPart == false)
            this.currentPart++;
        if (this.currentPart == 2 && this.config.minutePart == false)
            this.currentPart++;
        if (this.currentPart == 3)
            this.selected.emit(this.getDate());
    }
    getAngleFromClick(event) {
        let x = event.layerX - (event.target.clientWidth / 2);
        let y = event.layerY - (event.target.clientHeight / 2);
        let angle = 0;
        if (x == 0) {
            if (y <= 0)
                angle = 180;
            else
                angle = 0;
        }
        else {
            angle = 57.29 * Math.atan(y / x);
            if (x < 0)
                angle = angle + 270;
            else
                angle = angle + 90;
        }
        return angle;
    }
};
RbPopupDatetimeComponent.ctorParameters = () => [
    { type: DateTimePopupConfig, decorators: [{ type: _angular_core__WEBPACK_IMPORTED_MODULE_1__["Inject"], args: [app_tokens__WEBPACK_IMPORTED_MODULE_2__["CONTAINER_DATA"],] }] },
    { type: _angular_cdk_overlay__WEBPACK_IMPORTED_MODULE_3__["OverlayRef"] },
    { type: app_data_service__WEBPACK_IMPORTED_MODULE_4__["DataService"] }
];
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Output"])()
], RbPopupDatetimeComponent.prototype, "selected", void 0);
RbPopupDatetimeComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
        selector: 'rb-popup-datetime',
        template: __webpack_require__(/*! raw-loader!./rb-popup-datetime.component.html */ "./node_modules/raw-loader/index.js!./src/app/rb-popup-datetime/rb-popup-datetime.component.html"),
        styles: [__webpack_require__(/*! ./rb-popup-datetime.component.css */ "./src/app/rb-popup-datetime/rb-popup-datetime.component.css")]
    }),
    tslib__WEBPACK_IMPORTED_MODULE_0__["__param"](0, Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Inject"])(app_tokens__WEBPACK_IMPORTED_MODULE_2__["CONTAINER_DATA"]))
], RbPopupDatetimeComponent);



/***/ }),

/***/ "./src/app/rb-popup-list/rb-popup-list.component.css":
/*!***********************************************************!*\
  !*** ./src/app/rb-popup-list/rb-popup-list.component.css ***!
  \***********************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = ":host {\n    background: white;\n    border-radius: 4px;\n    box-shadow: 0 7px 8px -4px rgba(0, 0, 0, 0.2),\n        0 13px 19px 2px rgba(0, 0, 0, 0.14),\n        0 5px 24px 4px rgba(0, 0, 0, 0.12);\n    display: flex;\n    flex-direction: column;\n    align-items: stretch;\n    min-width: 200px;\n}\n\n.mat-list {\n    padding: 0px;\n    flex-grow: 1;\n}\n\n.mat-list-item {\n    font-size: 16px;\n    padding: 0px;\n}\n\n:host /deep/ .mat-list-item-content {\n    padding: 0!important;\n}\n\n.mat-button {\n    font-size: 16px;\n    font-weight: initial;\n    height: 48px;\n}\n\n.rb-list-select-button {\n    flex-grow: 1;\n    text-align: left;\n}\n\n.rb-list-expand-button {\n\n}\n\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbInNyYy9hcHAvcmItcG9wdXAtbGlzdC9yYi1wb3B1cC1saXN0LmNvbXBvbmVudC5jc3MiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7SUFDSSxpQkFBaUI7SUFDakIsa0JBQWtCO0lBQ2xCOzswQ0FFc0M7SUFDdEMsYUFBYTtJQUNiLHNCQUFzQjtJQUN0QixvQkFBb0I7SUFDcEIsZ0JBQWdCO0FBQ3BCOztBQUVBO0lBQ0ksWUFBWTtJQUNaLFlBQVk7QUFDaEI7O0FBRUE7SUFDSSxlQUFlO0lBQ2YsWUFBWTtBQUNoQjs7QUFFQTtJQUNJLG9CQUFvQjtBQUN4Qjs7QUFFQTtJQUNJLGVBQWU7SUFDZixvQkFBb0I7SUFDcEIsWUFBWTtBQUNoQjs7QUFFQTtJQUNJLFlBQVk7SUFDWixnQkFBZ0I7QUFDcEI7O0FBRUE7O0FBRUEiLCJmaWxlIjoic3JjL2FwcC9yYi1wb3B1cC1saXN0L3JiLXBvcHVwLWxpc3QuY29tcG9uZW50LmNzcyIsInNvdXJjZXNDb250ZW50IjpbIjpob3N0IHtcbiAgICBiYWNrZ3JvdW5kOiB3aGl0ZTtcbiAgICBib3JkZXItcmFkaXVzOiA0cHg7XG4gICAgYm94LXNoYWRvdzogMCA3cHggOHB4IC00cHggcmdiYSgwLCAwLCAwLCAwLjIpLFxuICAgICAgICAwIDEzcHggMTlweCAycHggcmdiYSgwLCAwLCAwLCAwLjE0KSxcbiAgICAgICAgMCA1cHggMjRweCA0cHggcmdiYSgwLCAwLCAwLCAwLjEyKTtcbiAgICBkaXNwbGF5OiBmbGV4O1xuICAgIGZsZXgtZGlyZWN0aW9uOiBjb2x1bW47XG4gICAgYWxpZ24taXRlbXM6IHN0cmV0Y2g7XG4gICAgbWluLXdpZHRoOiAyMDBweDtcbn1cblxuLm1hdC1saXN0IHtcbiAgICBwYWRkaW5nOiAwcHg7XG4gICAgZmxleC1ncm93OiAxO1xufVxuXG4ubWF0LWxpc3QtaXRlbSB7XG4gICAgZm9udC1zaXplOiAxNnB4O1xuICAgIHBhZGRpbmc6IDBweDtcbn1cblxuOmhvc3QgL2RlZXAvIC5tYXQtbGlzdC1pdGVtLWNvbnRlbnQge1xuICAgIHBhZGRpbmc6IDAhaW1wb3J0YW50O1xufVxuXG4ubWF0LWJ1dHRvbiB7XG4gICAgZm9udC1zaXplOiAxNnB4O1xuICAgIGZvbnQtd2VpZ2h0OiBpbml0aWFsO1xuICAgIGhlaWdodDogNDhweDtcbn1cblxuLnJiLWxpc3Qtc2VsZWN0LWJ1dHRvbiB7XG4gICAgZmxleC1ncm93OiAxO1xuICAgIHRleHQtYWxpZ246IGxlZnQ7XG59XG5cbi5yYi1saXN0LWV4cGFuZC1idXR0b24ge1xuXG59XG4iXX0= */"

/***/ }),

/***/ "./src/app/rb-popup-list/rb-popup-list.component.ts":
/*!**********************************************************!*\
  !*** ./src/app/rb-popup-list/rb-popup-list.component.ts ***!
  \**********************************************************/
/*! exports provided: RbPopupListComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbPopupListComponent", function() { return RbPopupListComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");
/* harmony import */ var _angular_cdk_overlay__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/cdk/overlay */ "./node_modules/@angular/cdk/esm2015/overlay.js");
/* harmony import */ var _tokens__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../tokens */ "./src/app/tokens.ts");
/* harmony import */ var app_data_service__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! app/data.service */ "./src/app/data.service.ts");





let RbPopupListComponent = class RbPopupListComponent {
    constructor(config, overlayRef, dataService) {
        this.config = config;
        this.overlayRef = overlayRef;
        this.dataService = dataService;
        this.selected = new _angular_core__WEBPACK_IMPORTED_MODULE_1__["EventEmitter"]();
        this.expanded = new _angular_core__WEBPACK_IMPORTED_MODULE_1__["EventEmitter"]();
        this.colapsed = new _angular_core__WEBPACK_IMPORTED_MODULE_1__["EventEmitter"]();
        this.hierarchy = [];
        this.list = [];
    }
    ngOnInit() {
        this.search = "";
        this.getData();
    }
    get isHierarchical() {
        return (this.config.parentattribute != null && this.config.childattribute != null);
    }
    getData() {
        let filter = {};
        if (this.config.parentattribute != null) {
            if (this.hierarchy.length > 0) {
                const lastObject = this.hierarchy[this.hierarchy.length - 1];
                filter[this.config.parentattribute] = this.config.childattribute == 'uid' ? lastObject.uid : lastObject.data[this.config.childattribute];
            }
            else {
                filter[this.config.parentattribute] = null;
            }
        }
        this.isLoading = true;
        this.dataService.listRelatedObjects(this.config.rbObject.objectname, this.config.rbObject.uid, this.config.attribute, filter, this.search).subscribe(data => this.setData(data));
    }
    setData(objects) {
        this.list = objects;
        this.isLoading = false;
    }
    setSearch(str) {
        this.search = str;
        this.getData();
    }
    select(object) {
        this.selected.emit(object);
    }
    expand(object) {
        this.list = [];
        this.hierarchy.push(object);
        this.getData();
        this.expanded.emit(object);
    }
    colapse(object) {
        let i = this.hierarchy.indexOf(object);
        this.list = [];
        this.hierarchy.splice(i);
        this.getData();
        this.colapsed.emit(object);
    }
};
RbPopupListComponent.ctorParameters = () => [
    { type: undefined, decorators: [{ type: _angular_core__WEBPACK_IMPORTED_MODULE_1__["Inject"], args: [_tokens__WEBPACK_IMPORTED_MODULE_3__["CONTAINER_DATA"],] }] },
    { type: _angular_cdk_overlay__WEBPACK_IMPORTED_MODULE_2__["OverlayRef"] },
    { type: app_data_service__WEBPACK_IMPORTED_MODULE_4__["DataService"] }
];
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Output"])()
], RbPopupListComponent.prototype, "selected", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Output"])()
], RbPopupListComponent.prototype, "expanded", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Output"])()
], RbPopupListComponent.prototype, "colapsed", void 0);
RbPopupListComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
        selector: 'rb-popup-list',
        template: __webpack_require__(/*! raw-loader!./rb-popup-list.component.html */ "./node_modules/raw-loader/index.js!./src/app/rb-popup-list/rb-popup-list.component.html"),
        styles: [__webpack_require__(/*! ./rb-popup-list.component.css */ "./src/app/rb-popup-list/rb-popup-list.component.css")]
    }),
    tslib__WEBPACK_IMPORTED_MODULE_0__["__param"](0, Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Inject"])(_tokens__WEBPACK_IMPORTED_MODULE_3__["CONTAINER_DATA"]))
], RbPopupListComponent);



/***/ }),

/***/ "./src/app/rb-related-input/rb-related-input.component.css":
/*!*****************************************************************!*\
  !*** ./src/app/rb-related-input/rb-related-input.component.css ***!
  \*****************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL3JiLXJlbGF0ZWQtaW5wdXQvcmItcmVsYXRlZC1pbnB1dC5jb21wb25lbnQuY3NzIn0= */"

/***/ }),

/***/ "./src/app/rb-related-input/rb-related-input.component.ts":
/*!****************************************************************!*\
  !*** ./src/app/rb-related-input/rb-related-input.component.ts ***!
  \****************************************************************/
/*! exports provided: RbRelatedInputComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbRelatedInputComponent", function() { return RbRelatedInputComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");
/* harmony import */ var _angular_cdk_overlay__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/cdk/overlay */ "./node_modules/@angular/cdk/esm2015/overlay.js");
/* harmony import */ var _rb_popup_list_rb_popup_list_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../rb-popup-list/rb-popup-list.component */ "./src/app/rb-popup-list/rb-popup-list.component.ts");
/* harmony import */ var _angular_cdk_portal__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/cdk/portal */ "./node_modules/@angular/cdk/esm2015/portal.js");
/* harmony import */ var _tokens__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ../tokens */ "./src/app/tokens.ts");






let RbRelatedInputComponent = class RbRelatedInputComponent {
    constructor(injector, overlay, viewContainerRef) {
        this.injector = injector;
        this.overlay = overlay;
        this.viewContainerRef = viewContainerRef;
    }
    ngOnInit() {
    }
    get displayvalue() {
        if (this.overlayRef != null)
            return this.searchValue;
        if (this.rbObject != null && this.rbObject.related[this.attribute] != null)
            return this.rbObject.related[this.attribute].data[this.displayattribute];
        else
            return null;
    }
    set displayvalue(str) {
        this.searchValue = str;
        this.popupListComponentRef.instance.setSearch(this.searchValue);
    }
    get readonly() {
        if (this.rbObject != null && this.rbObject.validation[this.attribute] != null)
            return !(this.editable && this.rbObject.validation[this.attribute].editable);
        else
            return true;
    }
    openPopupList() {
        if (!this.readonly) {
            this.overlayRef = this.overlay.create({
                positionStrategy: this.overlay.position().connectedTo(this.inputContainerRef.element, { originX: 'start', originY: 'bottom' }, { overlayX: 'start', overlayY: 'top' }),
                hasBackdrop: true,
                backdropClass: 'cdk-overlay-transparent-backdrop'
            });
            this.overlayRef.backdropClick().subscribe(() => {
                this.cancelEditing();
            });
            const injectorTokens = new WeakMap();
            injectorTokens.set(_angular_cdk_overlay__WEBPACK_IMPORTED_MODULE_2__["OverlayRef"], this.overlayRef);
            injectorTokens.set(_tokens__WEBPACK_IMPORTED_MODULE_5__["CONTAINER_DATA"], {
                rbObject: this.rbObject,
                attribute: this.attribute,
                displayattribute: this.displayattribute,
                parentattribute: this.parentattribute,
                childattribute: this.childattribute
            });
            let inj = new _angular_cdk_portal__WEBPACK_IMPORTED_MODULE_4__["PortalInjector"](this.injector, injectorTokens);
            const popupListPortal = new _angular_cdk_portal__WEBPACK_IMPORTED_MODULE_4__["ComponentPortal"](_rb_popup_list_rb_popup_list_component__WEBPACK_IMPORTED_MODULE_3__["RbPopupListComponent"], this.viewContainerRef, inj);
            this.popupListComponentRef = this.overlayRef.attach(popupListPortal);
            this.popupListComponentRef.instance.selected.subscribe(object => this.selected(object));
        }
    }
    focus(event) {
        if (this.overlayRef == null)
            this.openPopupList();
    }
    blur(event) {
        if (this.overlayRef != null)
            this.inputContainerRef.element.nativeElement.focus();
    }
    keydown(event) {
        if (event.keyCode == 13) {
            this.selected(this.highlightedObject);
        }
        else if (event.keyCode == 9 || event.keyCode == 27) {
            this.cancelEditing();
        }
    }
    cancelEditing() {
        this.overlayRef.dispose();
        this.overlayRef = null;
        this.searchValue = '';
        this.inputContainerRef.element.nativeElement.blur();
    }
    selected(object) {
        let link = this.rbObject.validation[this.attribute].related.link;
        let val = (link == 'uid') ? object.uid : object.data[link];
        this.rbObject.setValueAndRelated(this.attribute, val, object);
        this.overlayRef.dispose();
        this.overlayRef = null;
        this.searchValue = '';
        this.inputContainerRef.element.nativeElement.blur();
    }
};
RbRelatedInputComponent.ctorParameters = () => [
    { type: _angular_core__WEBPACK_IMPORTED_MODULE_1__["Injector"] },
    { type: _angular_cdk_overlay__WEBPACK_IMPORTED_MODULE_2__["Overlay"] },
    { type: _angular_core__WEBPACK_IMPORTED_MODULE_1__["ViewContainerRef"] }
];
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('label')
], RbRelatedInputComponent.prototype, "label", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('icon')
], RbRelatedInputComponent.prototype, "icon", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('size')
], RbRelatedInputComponent.prototype, "size", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('editable')
], RbRelatedInputComponent.prototype, "editable", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('object')
], RbRelatedInputComponent.prototype, "rbObject", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('attribute')
], RbRelatedInputComponent.prototype, "attribute", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('displayattribute')
], RbRelatedInputComponent.prototype, "displayattribute", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('parentattribute')
], RbRelatedInputComponent.prototype, "parentattribute", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('childattribute')
], RbRelatedInputComponent.prototype, "childattribute", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["ViewChild"])('input', { read: _angular_core__WEBPACK_IMPORTED_MODULE_1__["ViewContainerRef"], static: false })
], RbRelatedInputComponent.prototype, "inputContainerRef", void 0);
RbRelatedInputComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
        selector: 'rb-related-input',
        template: __webpack_require__(/*! raw-loader!./rb-related-input.component.html */ "./node_modules/raw-loader/index.js!./src/app/rb-related-input/rb-related-input.component.html"),
        styles: [__webpack_require__(/*! ./rb-related-input.component.css */ "./src/app/rb-related-input/rb-related-input.component.css")]
    })
], RbRelatedInputComponent);



/***/ }),

/***/ "./src/app/rb-search/rb-search.component.css":
/*!***************************************************!*\
  !*** ./src/app/rb-search/rb-search.component.css ***!
  \***************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL3JiLXNlYXJjaC9yYi1zZWFyY2guY29tcG9uZW50LmNzcyJ9 */"

/***/ }),

/***/ "./src/app/rb-search/rb-search.component.ts":
/*!**************************************************!*\
  !*** ./src/app/rb-search/rb-search.component.ts ***!
  \**************************************************/
/*! exports provided: RbSearchComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbSearchComponent", function() { return RbSearchComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");


let RbSearchComponent = class RbSearchComponent {
    constructor() { }
    ngOnInit() {
    }
};
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('icon')
], RbSearchComponent.prototype, "icon", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('size')
], RbSearchComponent.prototype, "size", void 0);
RbSearchComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
        selector: 'rb-search',
        template: __webpack_require__(/*! raw-loader!./rb-search.component.html */ "./node_modules/raw-loader/index.js!./src/app/rb-search/rb-search.component.html"),
        styles: [__webpack_require__(/*! ./rb-search.component.css */ "./src/app/rb-search/rb-search.component.css")]
    })
], RbSearchComponent);



/***/ }),

/***/ "./src/app/rb-tab-section/rb-tab-section.directive.ts":
/*!************************************************************!*\
  !*** ./src/app/rb-tab-section/rb-tab-section.directive.ts ***!
  \************************************************************/
/*! exports provided: RbTabSectionDirective */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbTabSectionDirective", function() { return RbTabSectionDirective; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");


let RbTabSectionDirective = class RbTabSectionDirective {
    constructor() {
        this.tabs = [];
    }
    register(tab) {
        this.tabs.push(tab);
    }
    select(tab) {
        this.visibleTab = tab;
    }
    isTabVisible(tab) {
        return (this.active && this.visibleTab != null && this.visibleTab == tab);
    }
};
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('active')
], RbTabSectionDirective.prototype, "active", void 0);
RbTabSectionDirective = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Directive"])({
        selector: 'rb-tab-section',
        exportAs: 'tabsection'
    })
], RbTabSectionDirective);



/***/ }),

/***/ "./src/app/rb-tab/rb-tab.directive.ts":
/*!********************************************!*\
  !*** ./src/app/rb-tab/rb-tab.directive.ts ***!
  \********************************************/
/*! exports provided: RbTabDirective */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbTabDirective", function() { return RbTabDirective; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");


let RbTabDirective = class RbTabDirective {
    constructor() {
        this.register = new _angular_core__WEBPACK_IMPORTED_MODULE_1__["EventEmitter"]();
    }
    ngOnInit() {
        this.register.emit(this);
    }
};
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('id')
], RbTabDirective.prototype, "id", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('label')
], RbTabDirective.prototype, "label", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('active')
], RbTabDirective.prototype, "active", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Output"])()
], RbTabDirective.prototype, "register", void 0);
RbTabDirective = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Directive"])({
        selector: 'rb-tab',
        exportAs: 'tab'
    })
], RbTabDirective);



/***/ }),

/***/ "./src/app/rb-textarea-input/rb-textarea-input.component.css":
/*!*******************************************************************!*\
  !*** ./src/app/rb-textarea-input/rb-textarea-input.component.css ***!
  \*******************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL3JiLXRleHRhcmVhLWlucHV0L3JiLXRleHRhcmVhLWlucHV0LmNvbXBvbmVudC5jc3MifQ== */"

/***/ }),

/***/ "./src/app/rb-textarea-input/rb-textarea-input.component.ts":
/*!******************************************************************!*\
  !*** ./src/app/rb-textarea-input/rb-textarea-input.component.ts ***!
  \******************************************************************/
/*! exports provided: RbTextareaInputComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbTextareaInputComponent", function() { return RbTextareaInputComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");


let RbTextareaInputComponent = class RbTextareaInputComponent {
    constructor() { }
    ngOnInit() {
    }
    get value() {
        if (this.rbObject != null)
            return this.rbObject.data[this.attribute];
        else
            return null;
    }
    set value(val) {
        this.editedValue = val;
    }
    get readonly() {
        if (this.rbObject != null && this.rbObject.validation[this.attribute] != null)
            return !(this.editable && this.rbObject.validation[this.attribute].editable);
        else
            return true;
    }
    commit() {
        this.rbObject.setValue(this.attribute, this.editedValue);
    }
};
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('label')
], RbTextareaInputComponent.prototype, "label", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('icon')
], RbTextareaInputComponent.prototype, "icon", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('size')
], RbTextareaInputComponent.prototype, "size", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('editable')
], RbTextareaInputComponent.prototype, "editable", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('object')
], RbTextareaInputComponent.prototype, "rbObject", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('attribute')
], RbTextareaInputComponent.prototype, "attribute", void 0);
RbTextareaInputComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
        selector: 'rb-textarea-input',
        template: __webpack_require__(/*! raw-loader!./rb-textarea-input.component.html */ "./node_modules/raw-loader/index.js!./src/app/rb-textarea-input/rb-textarea-input.component.html"),
        styles: [__webpack_require__(/*! ./rb-textarea-input.component.css */ "./src/app/rb-textarea-input/rb-textarea-input.component.css")]
    })
], RbTextareaInputComponent);



/***/ }),

/***/ "./src/app/rb-view-loader/rb-view-loader.component.css":
/*!*************************************************************!*\
  !*** ./src/app/rb-view-loader/rb-view-loader.component.css ***!
  \*************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL3JiLXZpZXctbG9hZGVyL3JiLXZpZXctbG9hZGVyLmNvbXBvbmVudC5jc3MifQ== */"

/***/ }),

/***/ "./src/app/rb-view-loader/rb-view-loader.component.ts":
/*!************************************************************!*\
  !*** ./src/app/rb-view-loader/rb-view-loader.component.ts ***!
  \************************************************************/
/*! exports provided: RbViewLoaderComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RbViewLoaderComponent", function() { return RbViewLoaderComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");
/* harmony import */ var _angular_http__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/http */ "./node_modules/@angular/http/fesm2015/http.js");
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/common */ "./node_modules/@angular/common/fesm2015/common.js");
/* harmony import */ var _redback_module__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../redback.module */ "./src/app/redback.module.ts");





let RbViewLoaderComponent = class RbViewLoaderComponent {
    constructor(http, resolver, compiler, vcRef) {
        this.http = http;
        this.resolver = resolver;
        this.compiler = compiler;
        this.vcRef = vcRef;
        this.navigate = new _angular_core__WEBPACK_IMPORTED_MODULE_1__["EventEmitter"]();
    }
    ngOnInit() {
    }
    ngOnChanges(changes) {
        this.http.get(this.templateUrl, { withCredentials: true, responseType: 0 }).subscribe(res => this.compileTemplate(res.text()));
    }
    compileTemplate(body) {
        let ViewComponent = class ViewComponent {
            constructor() {
                this.navigate = new _angular_core__WEBPACK_IMPORTED_MODULE_1__["EventEmitter"]();
            }
            navigateTo(view, title) {
                this.navigate.emit({ view: view, title: title });
            }
        };
        tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
            Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Output"])()
        ], ViewComponent.prototype, "navigate", void 0);
        ViewComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
            Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
                selector: 'rb-view',
                template: body
            })
        ], ViewComponent);
        ;
        let RuntimeComponentModule = class RuntimeComponentModule {
        };
        RuntimeComponentModule = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
            Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["NgModule"])({
                imports: [
                    _angular_common__WEBPACK_IMPORTED_MODULE_3__["CommonModule"],
                    _redback_module__WEBPACK_IMPORTED_MODULE_4__["RedbackModule"]
                ],
                declarations: [
                    ViewComponent
                ]
            })
        ], RuntimeComponentModule);
        let module = this.compiler.compileModuleAndAllComponentsSync(RuntimeComponentModule);
        let factory = module.componentFactories.find(f => f.componentType === ViewComponent);
        if (this.componentRef) {
            this.componentRef.destroy();
            this.componentRef = null;
        }
        let newViewComponentRef = this.container.createComponent(factory);
        newViewComponentRef.instance.navigate.subscribe(e => this.navigateTo(e));
        this.componentRef = newViewComponentRef;
    }
    navigateTo($event) {
        this.navigate.emit($event);
    }
};
RbViewLoaderComponent.ctorParameters = () => [
    { type: _angular_http__WEBPACK_IMPORTED_MODULE_2__["Http"] },
    { type: _angular_core__WEBPACK_IMPORTED_MODULE_1__["ComponentFactoryResolver"] },
    { type: _angular_core__WEBPACK_IMPORTED_MODULE_1__["Compiler"] },
    { type: _angular_core__WEBPACK_IMPORTED_MODULE_1__["ViewContainerRef"] }
];
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])('src')
], RbViewLoaderComponent.prototype, "templateUrl", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["ViewChild"])('container', { read: _angular_core__WEBPACK_IMPORTED_MODULE_1__["ViewContainerRef"], static: false })
], RbViewLoaderComponent.prototype, "container", void 0);
tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Output"])()
], RbViewLoaderComponent.prototype, "navigate", void 0);
RbViewLoaderComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
        selector: 'rb-view-loader',
        template: __webpack_require__(/*! raw-loader!./rb-view-loader.component.html */ "./node_modules/raw-loader/index.js!./src/app/rb-view-loader/rb-view-loader.component.html"),
        styles: [__webpack_require__(/*! ./rb-view-loader.component.css */ "./src/app/rb-view-loader/rb-view-loader.component.css")]
    })
], RbViewLoaderComponent);



/***/ }),

/***/ "./src/app/redback.module.ts":
/*!***********************************!*\
  !*** ./src/app/redback.module.ts ***!
  \***********************************/
/*! exports provided: RedbackModule */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RedbackModule", function() { return RedbackModule; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ "./node_modules/@angular/common/fesm2015/common.js");
/* harmony import */ var _desktop_root_desktop_root_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./desktop-root/desktop-root.component */ "./src/app/desktop-root/desktop-root.component.ts");
/* harmony import */ var _angular_platform_browser_animations__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/platform-browser/animations */ "./node_modules/@angular/platform-browser/fesm2015/animations.js");
/* harmony import */ var _angular_material__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/material */ "./node_modules/@angular/material/esm2015/material.js");
/* harmony import */ var _angular_http__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @angular/http */ "./node_modules/@angular/http/fesm2015/http.js");
/* harmony import */ var _angular_common_http__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! @angular/common/http */ "./node_modules/@angular/common/fesm2015/http.js");
/* harmony import */ var ngx_cookie_service__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ngx-cookie-service */ "./node_modules/ngx-cookie-service/ngx-cookie-service.js");
/* harmony import */ var _rb_dataset_rb_dataset_directive__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! ./rb-dataset/rb-dataset.directive */ "./src/app/rb-dataset/rb-dataset.directive.ts");
/* harmony import */ var _rb_menu_rb_menu_directive__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! ./rb-menu/rb-menu.directive */ "./src/app/rb-menu/rb-menu.directive.ts");
/* harmony import */ var _rb_view_loader_rb_view_loader_component__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! ./rb-view-loader/rb-view-loader.component */ "./src/app/rb-view-loader/rb-view-loader.component.ts");
/* harmony import */ var _rb_list_scroll_rb_list_scroll_directive__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(/*! ./rb-list-scroll/rb-list-scroll.directive */ "./src/app/rb-list-scroll/rb-list-scroll.directive.ts");
/* harmony import */ var _api_service__WEBPACK_IMPORTED_MODULE_13__ = __webpack_require__(/*! ./api.service */ "./src/app/api.service.ts");
/* harmony import */ var _data_service__WEBPACK_IMPORTED_MODULE_14__ = __webpack_require__(/*! ./data.service */ "./src/app/data.service.ts");
/* harmony import */ var _rb_input_rb_input_component__WEBPACK_IMPORTED_MODULE_15__ = __webpack_require__(/*! ./rb-input/rb-input.component */ "./src/app/rb-input/rb-input.component.ts");
/* harmony import */ var _angular_forms__WEBPACK_IMPORTED_MODULE_16__ = __webpack_require__(/*! @angular/forms */ "./node_modules/@angular/forms/fesm2015/forms.js");
/* harmony import */ var _rb_related_input_rb_related_input_component__WEBPACK_IMPORTED_MODULE_17__ = __webpack_require__(/*! ./rb-related-input/rb-related-input.component */ "./src/app/rb-related-input/rb-related-input.component.ts");
/* harmony import */ var _rb_popup_list_rb_popup_list_component__WEBPACK_IMPORTED_MODULE_18__ = __webpack_require__(/*! ./rb-popup-list/rb-popup-list.component */ "./src/app/rb-popup-list/rb-popup-list.component.ts");
/* harmony import */ var _angular_cdk_overlay__WEBPACK_IMPORTED_MODULE_19__ = __webpack_require__(/*! @angular/cdk/overlay */ "./node_modules/@angular/cdk/esm2015/overlay.js");
/* harmony import */ var _rb_tab_rb_tab_directive__WEBPACK_IMPORTED_MODULE_20__ = __webpack_require__(/*! ./rb-tab/rb-tab.directive */ "./src/app/rb-tab/rb-tab.directive.ts");
/* harmony import */ var _rb_tab_section_rb_tab_section_directive__WEBPACK_IMPORTED_MODULE_21__ = __webpack_require__(/*! ./rb-tab-section/rb-tab-section.directive */ "./src/app/rb-tab-section/rb-tab-section.directive.ts");
/* harmony import */ var _rb_popup_datetime_rb_popup_datetime_component__WEBPACK_IMPORTED_MODULE_22__ = __webpack_require__(/*! ./rb-popup-datetime/rb-popup-datetime.component */ "./src/app/rb-popup-datetime/rb-popup-datetime.component.ts");
/* harmony import */ var _rb_datetime_input_rb_datetime_input_component__WEBPACK_IMPORTED_MODULE_23__ = __webpack_require__(/*! ./rb-datetime-input/rb-datetime-input.component */ "./src/app/rb-datetime-input/rb-datetime-input.component.ts");
/* harmony import */ var _rb_textarea_input_rb_textarea_input_component__WEBPACK_IMPORTED_MODULE_24__ = __webpack_require__(/*! ./rb-textarea-input/rb-textarea-input.component */ "./src/app/rb-textarea-input/rb-textarea-input.component.ts");
/* harmony import */ var _rb_search_rb_search_component__WEBPACK_IMPORTED_MODULE_25__ = __webpack_require__(/*! ./rb-search/rb-search.component */ "./src/app/rb-search/rb-search.component.ts");
/* harmony import */ var _rb_map_rb_map_component__WEBPACK_IMPORTED_MODULE_26__ = __webpack_require__(/*! ./rb-map/rb-map.component */ "./src/app/rb-map/rb-map.component.ts");
/* harmony import */ var _rb_duration_input_rb_duration_input_component__WEBPACK_IMPORTED_MODULE_27__ = __webpack_require__(/*! ./rb-duration-input/rb-duration-input.component */ "./src/app/rb-duration-input/rb-duration-input.component.ts");




























let RedbackModule = class RedbackModule {
};
RedbackModule = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["NgModule"])({
        imports: [
            _angular_common__WEBPACK_IMPORTED_MODULE_2__["CommonModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatToolbarModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatButtonModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatSidenavModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatListModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatIconModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatFormFieldModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatInputModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatCheckboxModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatRadioModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatSelectModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatExpansionModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatDialogModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatIconModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatDividerModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatProgressSpinnerModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatMenuModule"],
            _angular_cdk_overlay__WEBPACK_IMPORTED_MODULE_19__["OverlayModule"],
            _angular_forms__WEBPACK_IMPORTED_MODULE_16__["FormsModule"],
            _angular_platform_browser_animations__WEBPACK_IMPORTED_MODULE_4__["BrowserAnimationsModule"],
            _angular_common_http__WEBPACK_IMPORTED_MODULE_7__["HttpClientModule"],
            _angular_http__WEBPACK_IMPORTED_MODULE_6__["HttpModule"]
        ],
        declarations: [
            _desktop_root_desktop_root_component__WEBPACK_IMPORTED_MODULE_3__["DesktopRootComponent"],
            _rb_dataset_rb_dataset_directive__WEBPACK_IMPORTED_MODULE_9__["RbDatasetDirective"],
            _rb_view_loader_rb_view_loader_component__WEBPACK_IMPORTED_MODULE_11__["RbViewLoaderComponent"],
            _rb_list_scroll_rb_list_scroll_directive__WEBPACK_IMPORTED_MODULE_12__["RbListScrollDirective"],
            _rb_input_rb_input_component__WEBPACK_IMPORTED_MODULE_15__["RbInputComponent"],
            _rb_textarea_input_rb_textarea_input_component__WEBPACK_IMPORTED_MODULE_24__["RbTextareaInputComponent"],
            _rb_related_input_rb_related_input_component__WEBPACK_IMPORTED_MODULE_17__["RbRelatedInputComponent"],
            _rb_datetime_input_rb_datetime_input_component__WEBPACK_IMPORTED_MODULE_23__["RbDatetimeInputComponent"],
            _rb_duration_input_rb_duration_input_component__WEBPACK_IMPORTED_MODULE_27__["RbDurationInputComponent"],
            _rb_popup_list_rb_popup_list_component__WEBPACK_IMPORTED_MODULE_18__["RbPopupListComponent"],
            _rb_popup_datetime_rb_popup_datetime_component__WEBPACK_IMPORTED_MODULE_22__["RbPopupDatetimeComponent"],
            _rb_search_rb_search_component__WEBPACK_IMPORTED_MODULE_25__["RbSearchComponent"],
            _rb_map_rb_map_component__WEBPACK_IMPORTED_MODULE_26__["RbMapComponent"],
            _rb_menu_rb_menu_directive__WEBPACK_IMPORTED_MODULE_10__["RbMenuDirective"],
            _rb_tab_rb_tab_directive__WEBPACK_IMPORTED_MODULE_20__["RbTabDirective"],
            _rb_tab_section_rb_tab_section_directive__WEBPACK_IMPORTED_MODULE_21__["RbTabSectionDirective"]
        ],
        exports: [
            _desktop_root_desktop_root_component__WEBPACK_IMPORTED_MODULE_3__["DesktopRootComponent"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatList"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatListItem"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatIconModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatDividerModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatButtonModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatProgressSpinnerModule"],
            _angular_material__WEBPACK_IMPORTED_MODULE_5__["MatMenuModule"],
            _rb_dataset_rb_dataset_directive__WEBPACK_IMPORTED_MODULE_9__["RbDatasetDirective"],
            _rb_view_loader_rb_view_loader_component__WEBPACK_IMPORTED_MODULE_11__["RbViewLoaderComponent"],
            _rb_list_scroll_rb_list_scroll_directive__WEBPACK_IMPORTED_MODULE_12__["RbListScrollDirective"],
            _rb_input_rb_input_component__WEBPACK_IMPORTED_MODULE_15__["RbInputComponent"],
            _rb_textarea_input_rb_textarea_input_component__WEBPACK_IMPORTED_MODULE_24__["RbTextareaInputComponent"],
            _rb_related_input_rb_related_input_component__WEBPACK_IMPORTED_MODULE_17__["RbRelatedInputComponent"],
            _rb_datetime_input_rb_datetime_input_component__WEBPACK_IMPORTED_MODULE_23__["RbDatetimeInputComponent"],
            _rb_duration_input_rb_duration_input_component__WEBPACK_IMPORTED_MODULE_27__["RbDurationInputComponent"],
            _rb_popup_list_rb_popup_list_component__WEBPACK_IMPORTED_MODULE_18__["RbPopupListComponent"],
            _rb_popup_datetime_rb_popup_datetime_component__WEBPACK_IMPORTED_MODULE_22__["RbPopupDatetimeComponent"],
            _rb_search_rb_search_component__WEBPACK_IMPORTED_MODULE_25__["RbSearchComponent"],
            _rb_map_rb_map_component__WEBPACK_IMPORTED_MODULE_26__["RbMapComponent"],
            _rb_menu_rb_menu_directive__WEBPACK_IMPORTED_MODULE_10__["RbMenuDirective"],
            _rb_tab_rb_tab_directive__WEBPACK_IMPORTED_MODULE_20__["RbTabDirective"],
            _rb_tab_section_rb_tab_section_directive__WEBPACK_IMPORTED_MODULE_21__["RbTabSectionDirective"]
        ],
        providers: [
            ngx_cookie_service__WEBPACK_IMPORTED_MODULE_8__["CookieService"],
            _api_service__WEBPACK_IMPORTED_MODULE_13__["ApiService"],
            _data_service__WEBPACK_IMPORTED_MODULE_14__["DataService"]
        ],
        entryComponents: [
            _rb_popup_list_rb_popup_list_component__WEBPACK_IMPORTED_MODULE_18__["RbPopupListComponent"],
            _rb_popup_datetime_rb_popup_datetime_component__WEBPACK_IMPORTED_MODULE_22__["RbPopupDatetimeComponent"]
        ],
        bootstrap: []
    })
], RedbackModule);



/***/ }),

/***/ "./src/app/tokens.ts":
/*!***************************!*\
  !*** ./src/app/tokens.ts ***!
  \***************************/
/*! exports provided: CONTAINER_DATA */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "CONTAINER_DATA", function() { return CONTAINER_DATA; });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");

const CONTAINER_DATA = new _angular_core__WEBPACK_IMPORTED_MODULE_0__["InjectionToken"]('CONTAINER_DATA');


/***/ }),

/***/ "./src/environments/environment.ts":
/*!*****************************************!*\
  !*** ./src/environments/environment.ts ***!
  \*****************************************/
/*! exports provided: environment */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "environment", function() { return environment; });
// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.
const environment = {
    production: false
};
/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.


/***/ }),

/***/ "./src/main.ts":
/*!*********************!*\
  !*** ./src/main.ts ***!
  \*********************/
/*! no exports provided */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var hammerjs__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! hammerjs */ "./node_modules/hammerjs/hammer.js");
/* harmony import */ var hammerjs__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(hammerjs__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm2015/core.js");
/* harmony import */ var _angular_platform_browser_dynamic__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/platform-browser-dynamic */ "./node_modules/@angular/platform-browser-dynamic/fesm2015/platform-browser-dynamic.js");
/* harmony import */ var _app_app_module__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./app/app.module */ "./src/app/app.module.ts");
/* harmony import */ var _environments_environment__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ./environments/environment */ "./src/environments/environment.ts");





if (_environments_environment__WEBPACK_IMPORTED_MODULE_4__["environment"].production) {
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["enableProdMode"])();
}
Object(_angular_platform_browser_dynamic__WEBPACK_IMPORTED_MODULE_2__["platformBrowserDynamic"])().bootstrapModule(_app_app_module__WEBPACK_IMPORTED_MODULE_3__["AppModule"])
    .catch(err => console.error(err));


/***/ }),

/***/ 0:
/*!***************************!*\
  !*** multi ./src/main.ts ***!
  \***************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__(/*! C:\Users\ngron\git\redback\RedbackNg\src\main.ts */"./src/main.ts");


/***/ })

},[[0,"runtime","vendor"]]]);
//# sourceMappingURL=main-es2015.js.map