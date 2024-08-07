export const defaultGlobalPrefs = [
    {
        code:"uialt", 
        label: "UI Styles", 
        icon: "brush",
        options:[
            {
                value:"primary", 
                label: "Primary"
            }, 
            {
                value:"alt1", 
                label: "Alternate 1"
            }, 
            {
                value:"alt2", 
                label: "Alternate 2"
            }
        ]
    },
    {
        code:"notifgroup", 
        label: "Notification Grouping", 
        icon: "notifications",
        options:[
            {
                value:"nogroup", 
                label: "No Grouping"
            }, 
            {
                value:"byaction", 
                label: "By Action"
            }, 
            {
                value:"byobject", 
                label: "By Object"
            }
        ]
    },
    {
        code:"dateformat", 
        label: "Date Format", 
        icon: "calendar_month",
        options:[
            {
                value:"iso", 
                label: "YYYY-MM-DD"
            }, 
            {
                value:"isorev", 
                label: "DD/MM/YYY"
            }, 
            {
                value:"american", 
                label: "MM/DD/YYY"
            }
        ]
    },
    {
        code:"timeformat", 
        label: "Time Format", 
        icon: "schedule",
        options:[
            {
                value:"iso", 
                label: "24h"
            }, 
            {
                value:"ampm", 
                label: "AM/PM"
            }
        ]
    },
    {
        code:"timezone", 
        label: "Time Zone", 
        icon: "public",
        options:[
            {
                value:"local", 
                label: "Local timezone"
            }, 
            {
                value:"gmt", 
                label: "GMT"
            }
        ]
    }
]