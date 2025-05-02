import { Injectable } from '@angular/core';
import { UserprefService } from './userpref.service';

@Injectable({
  providedIn: 'root'
})
export class LogService {
  entries: any[] = [];
  level: string = "INFO";

  constructor(
  ) { }

  error(str: string) {
    console.error(str);
    this.addEntry("ERROR", str);
  }

  info(str: string) {
    console.log(str);
    this.addEntry("INFO", str);
  }

  debug(str: string) {
    if(this.level == 'DEBUG') {
      console.log(str);
      this.addEntry("DEBUG", str);
    }
  }

  addEntry(level: string, msg: string) {
    if(this.entries.length > 2100) {
      this.entries = this.entries.slice(this.entries.length - 2000, this.entries.length);
    }
    this.entries.push({
      ts: (new Date()).getTime(),
      level:level,
      msg: msg
    }); 
  }

  export() {
    const url = window.URL.createObjectURL(new Blob([...this.entries.map(e => `${e.ts}:${e.level}:${e.msg}\n`)]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'log.txt');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}
