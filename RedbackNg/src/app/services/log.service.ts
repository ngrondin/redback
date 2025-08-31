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
    this._err(str);
    this._addEntry("ERROR", str);
  }

  info(str: string) {
    this._log(str);
    this._addEntry("INFO", str);
  }

  debug(str: string) {
    if(this.level == 'DEBUG') {
      this._log(str);
      this._addEntry("DEBUG", str);
    }
  }

  _log(str: string) {
    let ts = (new Date()).getTime();
    console.log(ts + ": " + str);
  }

  _err(str: string) {
    let ts = (new Date()).getTime();
    console.error(ts + ": " + str);
  }

  _addEntry(level: string, msg: string) {
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
