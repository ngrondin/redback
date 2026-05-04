import { Observable, Observer } from "rxjs";
import { GanttMark, GanttMarkType, GanttTimeBasedConfig } from "./rb-gantt-models";
import { RbObject } from "app/datamodel";

export class GanttTimeConfig {
    monthNames: String[] = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    dayNames: String[] = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
    _zooms: any[] = [{label:"12 Hours", val:43200000}, {label:"1 Day", val:86400000}, {label:"2 Days", val:172800000}, {label:"3 Days", val:259200000}, {label:"7 Days", val:604800000}];
    _spans: any[] = [{label:"12 Hours", val:43200000}, {label:"1 Day", val:86400000}, {label:"3 Days", val:259200000}, {label:"7 Days", val:604800000}, {label:"14 Days", val:1209600000}];
    private startVariable?: string;
    private spanVariable?: string;
    private zoomVariable?: string;
    private displayElement: any;

    startMS: number = (new Date()).getTime();
    spanMS: number = 259200000;
    zoomMS: number = 172800000;
    markMajorIntervalMS: number = 3600000;
    markMinorIntervalMS: number = 900000;
    endMS: number = 0;
    pxPerMS: number = 1;
    widthPX: number = 1920;
    marks: GanttMark[] = [];
    private observers: Observer<void>[] = [];
    _startDate!: Date;

    constructor(cfg: any) {
        if(cfg.startVariable != null) this.startVariable = cfg.startVariable;
        if(cfg.spanVariable != null) this.spanVariable = cfg.spanVariable;
        if(cfg.zoomVariable != null)  this.zoomVariable = cfg.zoomVariable;
        if(cfg.element != null) this.displayElement = cfg.element;
        this.reset();
    }

    reset() {
        this._startDate = new Date();
        this.startMS = this._startDate.getTime();
        this.spanMS = 259200000;
        this.zoomMS = 172800000;
        if(this.startVariable != null) {
            let dtvar = window.redback[this.startVariable!];
            this._startDate = typeof dtvar == 'string' ? new Date(dtvar) : dtvar.getTime != null ? dtvar : this._startDate;
            this.startMS = this._startDate.getTime();
        } 
        if(this.spanVariable != null) {
            const spvar = window.redback[this.spanVariable!];
            this.spanMS = spvar != null ? parseInt(spvar) : this.spanMS;
        } 
        if(this.zoomVariable != null) {
            const zmvar = window.redback[this.zoomVariable!];
            this.zoomMS = Math.min(this.spanMS, zmvar);
        }  
        this.calc(false);    
    }

    get startDate(): Date {
        return this._startDate;
    }

    set startDate(dt: Date|string) {
        this._startDate = typeof dt == 'string' ? new Date(dt) : dt;
        this.startMS = this._startDate.getTime();
        if(this.startVariable != null) window.redback[this.startVariable] = this._startDate.toISOString();
        this.calc(true)
    }

    get endDate(): Date {
        return this.endMS != null ? new Date(this.endMS) : new Date();
    }

    get dayMarks() : GanttMark[] {
        return this.marks.filter(m => m.type == GanttMarkType.Day);
    }

    get majorMarks() : GanttMark[] {
        return this.marks.filter(m => m.type == GanttMarkType.Day || m.type == GanttMarkType.Major);
    }

    get zooms() : any[] {
        return this._zooms.filter(z => z.val <= this.spanMS);
    }

    set zoom(ms: number) {
        this.zoomMS = ms;
        if(this.zoomVariable != null) window.redback[this.zoomVariable] = this.zoomMS;
        this.calc(true)
    }

    setZoom(ms: number) {
        this.zoom = ms;
    }

    get spans() : any[] {
        return this._spans;
    }

    set span(ms: number) {
        this.spanMS = ms;
        if(this.spanVariable != null) window.redback[this.spanVariable] = this.spanMS;
        if(this.zoomMS != null && this.zoomMS > this.spanMS) this.zoomMS = this.spanMS;
        this.calc(true)
    }

    setSpan(ms: number) {
        this.span = ms;
    }

    private calc(publish: boolean) {
        this.endMS = this.startMS + this.spanMS;
        let clientWidthPX = this.displayElement != null ? this.displayElement.offsetWidth : 1920;
        this.pxPerMS = clientWidthPX / this.zoomMS
        this.widthPX = this.spanMS * this.pxPerMS;
        this.markMajorIntervalMS = 3600000;
        this.markMinorIntervalMS = 900000;
        while(this.markMajorIntervalMS * this.pxPerMS < 50) {
            this.markMajorIntervalMS *= 2;
            this.markMinorIntervalMS = this.markMajorIntervalMS;
        }
        this.marks = [];
        let lastMidnight = (new Date(this.startMS));
        lastMidnight.setHours(0);
        lastMidnight.setMinutes(0);
        lastMidnight.setSeconds(0);
        lastMidnight.setMilliseconds(0);
        let lastMidnightMS = lastMidnight.getTime();
        let cur = lastMidnightMS;
        while(cur < this.endMS) {
            let curDate: Date = new Date(cur);
            let sinceFirstMidnight = cur - lastMidnightMS;
            let pos = Math.round((cur - this.startMS) * this.pxPerMS);
            if(pos >= 0) {
                let dayLabel: string = this.dayNames[curDate.getDay()] + ", " + curDate.getDate() + " " + this.monthNames[curDate.getMonth()] + " " + curDate.getFullYear();
                let timeLabel: string = curDate.getHours() + ":00";
                let type: GanttMarkType = sinceFirstMidnight % 86400000 == 0 ? GanttMarkType.Day : sinceFirstMidnight % this.markMajorIntervalMS == 0 ? GanttMarkType.Major : GanttMarkType.Minor;
                this.marks.push(new GanttMark(pos, dayLabel, timeLabel, type));
            }
            cur = cur + this.markMinorIntervalMS;
        }
        let nowPos = Math.round((new Date().getTime() - this.startMS) * this.pxPerMS);
        if(nowPos > 0 && nowPos < this.spanMS) {
            this.marks.push(new GanttMark(nowPos, null, null, GanttMarkType.Now));
        }
        if(publish) this.publishChange();
    }

    private publishChange() {
        for(const observer of this.observers) {
            observer.next();
        }
    }

    changes() : Observable<void>  {
        return new Observable<any>((observer) => {
            this.observers.push(observer);
        });
    }

    getStartAndWidthPX(obj: RbObject, cfg: GanttTimeBasedConfig): [number|null, number|null] {
        let [startMS, endMS, durationMS] = cfg.getObjectStartEndDur(obj);
        let startPX: number = Math.round((startMS - this.startMS) * this.pxPerMS);
        if(startPX < this.widthPX) {
            if(startMS + durationMS > this.endMS) {
                durationMS = this.endMS - startMS;
            }
            let endMS = startMS + durationMS;
            let endPX = Math.round((endMS - this.startMS) * this.pxPerMS);
            let widthPX = endPX - startPX;
            if(startPX > -widthPX) {
                if(startPX < 0) {
                widthPX = widthPX + startPX;
                startPX = 0;
                }
                return [startPX, widthPX];
            } else {
                return [null, null];
            }
        } else {
            return [null, null];
        }
    }

    getWidthOfObject(obj: RbObject, cfg: GanttTimeBasedConfig): number {
        let [startMS, endMS, durationMS] = cfg.getObjectStartEndDur(obj);
        return Math.round(durationMS * this.pxPerMS);
    }

    getTimeOfLeftPX(px: number): number {
        return Math.round(this.startMS + (px / this.pxPerMS));
    }
}