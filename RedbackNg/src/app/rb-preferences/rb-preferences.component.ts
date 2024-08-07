import { Component, EventEmitter, Output } from '@angular/core';
import { UserprefService } from 'app/services/userpref.service';

@Component({
  selector: 'rb-preferences',
  templateUrl: './rb-preferences.component.html',
  styleUrls: ['./rb-preferences.component.css']
})
export class RbPreferencesComponent {
  @Output() close: EventEmitter<any> = new EventEmitter();

  constructor(
    public userprefService: UserprefService
  ) {}

  public get preferences() : any {
    return this.userprefService.getGlobalPreferences()
  }

  public getPreferenceValue(code: string): any {
    return this.userprefService.getGlobalPreferenceValue(code);
  }

  public setPreferenceValue(code: string, value: any) {
    this.userprefService.setGlobalPreferenceValue("user", code, value);
  }

  public closePreferences() {
    this.close.emit();
  }
}
