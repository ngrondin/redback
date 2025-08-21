import { Component, Input } from '@angular/core';

@Component({
  selector: 'rb-fileicon',
  templateUrl: './fileicon.component.html',
  styleUrls: ['./fileicon.component.css']
})
export class FileiconComponent {
  @Input('mime') mime: string;

  map = {
    "text/csv":{
      color: "#595",
      name: "CSV"
    },
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":{
      color: "#5a5",
      name: "XLS"
    },
    "application/vnd.ms-excel": {
      color: "#5a5",
      name: "XLS"      
    }    
  }

  get name() {
    let cfg = this.map[this.mime];
    return cfg != null ? cfg.name : "File";
  }

  get color() {
    let cfg = this.map[this.mime];
    return cfg != null ? cfg.color : "#888";
  }

}
