import { Component, Input } from '@angular/core';
import { RbDataCalcComponent } from 'app/abstract/rb-datacalc';
import { RbObject } from 'app/datamodel';
import { DataService } from 'app/services/data.service';
import { DragService } from 'app/services/drag.service';
import { FilterService } from 'app/services/filter.service';
import { ModalService } from 'app/services/modal.service';
import { UserprefService } from 'app/services/userpref.service';
import { ActionService } from 'app/services/action.service';
import { finalize } from 'rxjs';
import { DynamicFormEditorCategory, DynamicFormEditorCategorySeriesConfig, DynamicFormEditorItem, DynamicFormEditorItemSeriesConfig } from './rb-dynamicformeditor-models';
import { DialogService } from 'app/services/dialog.service';

@Component({
  selector: 'rb-dynamicformeditor',
  templateUrl: './rb-dynamicformeditor.component.html',
  styleUrls: ['./rb-dynamicformeditor.component.css']
})
export class RbDynamicformeditorComponent extends RbDataCalcComponent<DynamicFormEditorItemSeriesConfig> {
  @Input('items') itemsInput : any;
  @Input('categories') categoriesInput : any;

  categoryConfig: DynamicFormEditorCategorySeriesConfig;
  types: any[] = [
    {name: "Category",    value: "category",  icon: "crop_16_9",      is: "cat"},
    {name: "Short Text",  value: "string",    icon: "short_text",     is: "item"},
    {name: "Text Area",   value: "textarea",  icon: "edit_note",      is: "item"},
    {name: "Address",     value: "address",   icon: "home",           is: "item"},
    {name: "Phone",       value: "phone",     icon: "phone",          is: "item"},
    {name: "Email",       value: "email",     icon: "mail",           is: "item"},
    {name: "Checkbox",    value: "checkbox",  icon: "check_box",      is: "item"},
    {name: "Choice",      value: "choice",    icon: "list",           is: "item"},
    {name: "Date",        value: "date",      icon: "calendar_month", is: "item"},
    {name: "Number",      value: "number",    icon: "pin",            is: "item"},
    {name: "Photo",       value: "photos",    icon: "photo_camera",   is: "item"},
    {name: "Video",       value: "videos",    icon: "videocam",       is: "item"},
    {name: "Files",       value: "files",     icon: "attachment",     is: "item"},
    {name: "Signature",   value: "signature", icon: "draw",           is: "item"},
    {name: "Info Only",   value: "infoonly",  icon: "info",           is: "item"}    
  ];
  data: DynamicFormEditorCategory[];
  overDZ: any = null;
  
  constructor(
    private modalService: ModalService,
    private dragService: DragService,
    private userprefService: UserprefService,
    private filterService: FilterService,
    private dataService: DataService,
    private dialogService: DialogService
  ) {
    super();
  }

  dataCalcInit() {
    this.seriesConfigs = [];
    this.seriesConfigs.push(new DynamicFormEditorItemSeriesConfig(this.itemsInput));
    this.categoryConfig = new DynamicFormEditorCategorySeriesConfig(this.categoriesInput);
  }

  dataCalcDestroy() {

  }

  createSeriesConfig(json: any): DynamicFormEditorItemSeriesConfig {
    return null;
  }

  getFilterSortForSeries(config: DynamicFormEditorItemSeriesConfig) {

  }

  geIconForType(type) {
    let typeObj = this.types.find(t => t.value == type);
    if(typeObj != null) {
      return typeObj.icon;
    } else {
      return null;
    }
  }

  calc() {
    this.data = [];
    var catlist = [...this.datasetgroup.datasets[this.categoryConfig.dataset].list];
    catlist.sort((a, b) => a.get(this.categoryConfig.orderattribute) - b.get(this.categoryConfig.orderattribute));
    this.data.push(new DynamicFormEditorCategory(null, null));
    for(var catobj of catlist) {
      var cat = new DynamicFormEditorCategory(catobj, catobj.get(this.categoryConfig.labelattribute));
      this.data.push(cat);
    }
    for(var cat of this.data) {
      for(var cfg of this.seriesConfigs) {
        var list = [...this.datasetgroup.datasets[cfg.dataset].list];
        list = list.filter(rbo => rbo.get(cfg.categorylinkattribute) == (cat.object != null ? cat.object.uid : null));
        for(var rbo of list) {
          let item = new DynamicFormEditorItem(rbo, rbo.get(cfg.labelattribute), this.geIconForType(rbo.get(cfg.typeattribute)));
          cat.items.push(item);
        }
      }  
      cat.items.sort((a, b) => a.object.get(cfg.orderattribute) - b.object.get(cfg.orderattribute));
    }    
  }

  over(dz) {
    this.overDZ = dz;
  }

  out() {
    this.overDZ = null;
  }

  dropzoneClass(dz, activeClass, overClass) : string {
    if(this.dragService.isDragging && this.dragService.data.is == dz.type) {
      if(this.overDZ != null && this.overDZ.type == dz.type && this.overDZ.cat == dz.cat && this.overDZ.item == dz.item) {
        return overClass;
      } else {
        return activeClass;
      }
    } else {
      return "";
    }
  }

  dropped($event) {
    if(this.dragService.data.is == 'cat') {
      let cfg = this.categoryConfig;
      let dataset = this.datasetgroup.datasets[cfg.dataset];
      let list: RbObject[] = dataset.list.filter(o => o != this.dragService.data.object);
      list.sort((a, b) => a.get(cfg.orderattribute) - b.get(cfg.orderattribute));
      let newOrder = 1, o = 1;
      if(this.overDZ.cat.object == null) {
        newOrder = 1;
        o++;
      }
      for(var obj of list) {
        obj.setValue(cfg.orderattribute, o);
        o++;
        if(this.overDZ.cat != null && this.overDZ.cat.object == obj) {
          newOrder = o;
          o++;
        }
      }
      if(this.dragService.data.object == null) {
        let data = this.filterService.mergeFilters({}, dataset.resolvedFilter);
        data[cfg.orderattribute] = newOrder;
        this.dataService.create(dataset.objectname, null, data).subscribe(newObject => {
          this.redraw();
          dataset.select(newObject);
          this.modalService.open(cfg.modal);
        });  
      } else {
        this.dragService.data.object.setValue(cfg.orderattribute, newOrder);
        this.redraw();
      }
    } else if(this.dragService.data.is == 'item') {
      let dzCatUid = this.overDZ.cat != null && this.overDZ.cat.object != null ? this.overDZ.cat.object.uid : null
      let cfg = this.seriesConfigs[0];
      let dataset = this.datasetgroup.datasets[cfg.dataset];
      let list: RbObject[] = dataset.list.filter(o => o.get(cfg.categorylinkattribute) == dzCatUid && o != this.dragService.data.object);
      list.sort((a, b) => a.get(cfg.orderattribute) - b.get(cfg.orderattribute));
      let newOrder = 0, o = 0;
      if(this.overDZ.item == null) {
        newOrder = 0;
        o++;
      }
      for(var obj of list) {
        obj.setValue(cfg.orderattribute, o);
        o++;
        if(this.overDZ.item != null && this.overDZ.item.object == obj) {
          newOrder = o;
          o++;
        }
      }
      if(this.dragService.data.object == null) {
        let data = this.filterService.mergeFilters({}, dataset.resolvedFilter);
        data[cfg.typeattribute] = this.dragService.data.value;
        data[cfg.orderattribute] = newOrder;
        data[cfg.categorylinkattribute] = dzCatUid;
        this.dataService.create(dataset.objectname, null, data).subscribe(newObject => {
          this.redraw();
          dataset.select(newObject);
          this.modalService.open(cfg.modal);
        });  
      } else {
        this.dragService.data.object.setValue(cfg.categorylinkattribute, dzCatUid);
        this.dragService.data.object.setValue(cfg.orderattribute, newOrder);
        this.redraw();
      }
    }
  }

  edit(entity) {
    let cfg = null;
    if(entity.is == 'item') {
      cfg = this.seriesConfigs[0];
    } else if(entity.is == 'cat') {
      cfg = this.categoryConfig;
    }
    let dataset = this.datasetgroup.datasets[cfg.dataset];
    dataset.select(entity.object);
    this.modalService.open(cfg.modal);
  }

  delete(entity) {
    this.dialogService.openDialog(
      (entity.is == 'cat' ? "Delete this category? All contained items will also be deleted." : "Delete this item?"), 
      [
        {
          label: "Yes", 
          callback: () => this._delete(entity)
        }, 
        {
          label: "No", 
          callback: () => {}
        }
      ]
    );    
  }  

  private _delete(entity) {
    let cfg = null;
    if(entity.is == 'item') {
      cfg = this.seriesConfigs[0];
    } else if(entity.is == 'cat') {
      let itemcfg = this.seriesConfigs[0];
      let itemdataset = this.datasetgroup.datasets[itemcfg.dataset];
      let list: RbObject[] = itemdataset.list.filter(o => o.get(itemcfg.categorylinkattribute) == entity.object.uid);
      for(var obj of list) {
        itemdataset.delete(obj);
      }
      cfg = this.categoryConfig;
    }
    let dataset = this.datasetgroup.datasets[cfg.dataset];
    dataset.delete(entity.object);    
  }
}

