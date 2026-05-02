import { GanttMark } from "./rb-gantt-models";

export class GanttTimeConfig {
    _startDate: Date = new Date();
    spanMS: number | null = null;
    zoomMS: number | null = null;
    markMajorIntervalMS: number | null = null;
    markMinorIntervalMS: number | null = null;
    startMS: number | null = null;
    endMS: number | null = null;
    pxPerMS: number | null = null;
    widthPX: number | null = null;
    heightPX: number | null = null;

    monthNames: String[] = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    dayNames: String[] = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
    _zooms: any[] = [{label:"12 Hours", val:43200000}, {label:"1 Day", val:86400000}, {label:"2 Days", val:172800000}, {label:"3 Days", val:259200000}, {label:"7 Days", val:604800000}];
    _spans: any[] = [{label:"12 Hours", val:43200000}, {label:"1 Day", val:86400000}, {label:"3 Days", val:259200000}, {label:"7 Days", val:604800000}, {label:"14 Days", val:1209600000}];
    marks: GanttMark[] = [];

    constructor() {
        
    }
}