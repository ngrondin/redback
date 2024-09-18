import { RbObject } from "./datamodel";
import { Hasher } from "./helpers";
import { FilterService } from "./services/filter.service";



class DeferredFetchQueueItem {
    objectname: string;
    uid: string; 
    filter: any;
    promises: any[];
    checkCount: number = 0;
  
    constructor(on: string, i: string, f: any) {
      this.objectname = on;
      this.uid = i;
      this.filter = f;
      this.promises = [];
    }
  
    resolve(val: any) {
        console.log("Resolving " + this.promises.length + " promises for " + this.objectname);
        for(var promise of this.promises) promise.resolve(val);
    }
  
    reject(err: any) {
      for(var promise of this.promises) promise.reject(err);
    }
  }
  
  export class DeferredFetchQueue {
    index: {[key:string]:{[key:string]: {[key:string]:DeferredFetchQueueItem}}} = {}
  
    constructor(private filterService: FilterService) {}
  
    private getWithUID(name: string, uid: string): DeferredFetchQueueItem {
      if(this.index[name] == null) this.index[name] = {uids:{}, filters:{}};
      return this.index[name].uids[uid];
    }
  
    private getWithFilterHash(name: string, hash: number): DeferredFetchQueueItem {
      if(this.index[name] == null) this.index[name] = {uids:{}, filters:{}};
      return this.index[name].filters[hash];
    }
  
    fetchUid(name: string, uid: string): Promise<RbObject> {
      console.log("Fetching " + name + ":" + uid);
      let item = this.getWithUID(name, uid);
      if(item == null) {
        item = new DeferredFetchQueueItem(name, uid, null);
        this.index[name].uids[uid] = item;
      }
      return new Promise<RbObject>((resolve, reject) => {
        item.promises.push({resolve, reject});
      });
    }
  
    fetchFilter(name: string, filter: any): Promise<RbObject[]> {
      console.log("Fetching " + name + ":" + JSON.stringify(filter));
      let filterHash = Hasher.hash(filter);
      let item = this.getWithFilterHash(name, filterHash);
      if(item == null) {
        item = new DeferredFetchQueueItem(name, null, filter);
        this.index[name].filters[filterHash] = item;
      }
      return new Promise<RbObject[]>((resolve, reject) => {
        item.promises.push({resolve, reject});
      });
    }
  
    getMulti() : any[] {
      let multi = []
      for(let objectname of Object.keys(this.index)) {
        let objectgroup = this.index[objectname];
        let filter = null;
        let count = 0;
        let uids = Object.keys(objectgroup.uids); 
        if(uids.length > 0) {
          filter = {uid:{$in: uids}};
          count += uids.length;
        }
        let subfilter = Object.values(objectgroup.filters).map((i: any) => i.filter)
        if(subfilter.length > 0) {
          filter = {$or: (filter != null ? subfilter.concat(filter) : subfilter)};  
          count += subfilter.length * 2; //Times 2 in order to allow for domain overridden objects
        }
        if(filter != null) {
          multi.push({
            key: objectname,
            action: "list",
            object: objectname,
            filter: filter,
            page: 0,
            pagesize: count,
            options: {addvalidation: true, addrelated: false}
          });  
        }
      }
      console.log("GetMulti: " + JSON.stringify(multi));
      return multi;
    }
  
    resolve(objects: RbObject[]) {
      for(var objectname of Object.keys(this.index)) {
        let objectgroup = this.index[objectname];
        for(var uid of Object.keys(objectgroup.uids)) {
          let item = objectgroup.uids[uid];
          let object = objects.find(o => o.objectname == objectname && o.uid == uid);
          if(object != null) {
            item.resolve(object); 
            delete objectgroup.uids[uid];           
          } else {
            item.checkCount++;
            if(item.checkCount > 5) delete objectgroup.uids[uid];
          }
        }
        for(var hash of Object.keys(objectgroup.filters)) {
          let item = objectgroup.filters[hash];
          let list = objects.filter(o => o.objectname == objectname && this.filterService.applies(item.filter, o));
          if(list.length > 0) {
            item.resolve(list);
            delete objectgroup.filters[hash];  
          } else {
            item.checkCount++;
            if(item.checkCount > 5) delete objectgroup.filters[hash];
          }
        }
      }
    }
  
  }