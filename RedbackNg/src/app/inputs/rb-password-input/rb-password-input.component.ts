import { Component, Input } from '@angular/core';
import { AppInjector } from 'app/app.module';
import { ApiService } from 'app/services/api.service';
import { RbFieldInputComponent } from '../abstract/rb-field-input';

/**
 * An input for a secret value (password, token) kept by the secrets service, never in the object.
 *
 * At rest it shows only whether the secret is set, when and by whom. Clicking it gives an empty password box:
 * the typed value goes straight to the secrets service and the attribute gets the marker "(secret)".
 * The cross clears the secret and empties the attribute. The value itself never comes back to the browser.
 *
 * Config:
 * - attribute: the object attribute holding the marker
 * - secret: expression giving the secret name, for example "'intclient:' + object.get('name') + ':secret'"
 */
@Component({
  selector: 'rb-password-input',
  templateUrl: '../abstract/rb-field-input.html',
  styleUrls: ['../abstract/rb-field-input.css']
})
export class RbPasswordInputComponent extends RbFieldInputComponent {
  public static MARKER = '(secret)';
  @Input('secret') secret: string;
  apiService: ApiService;
  statusKey: string = null;
  statusText: string = null;

  constructor() {
    super();
    this.apiService = AppInjector.get(ApiService);
    this.defaultIcon = "key";
  }

  get secretName(): string {
    return this.rbObject != null && this.secret != null ? Function("object", "return (" + this.secret + ");")(this.rbObject) : null;
  }

  public getPersistedDisplayValue(): any {
    let name = this.secretName;
    let val = this.value;
    let key = this.rbObject != null ? this.rbObject.domain + "/" + name + "/" + val : null;
    if(key != this.statusKey) {
      this.statusKey = key;
      this.statusText = null;
      if(name != null) this.fetchStatus(this.rbObject.domain, name, val, key);
    }
    return this.statusText;
  }

  private fetchStatus(domain: string, name: string, val: any, key: string) {
    this.apiService.secretStatus(domain, name).subscribe({
      next: (resp) => {
        if(this.statusKey != key) return;
        if(resp.set == true) {
          let when = resp.updated != null ? new Date(resp.updated).toLocaleString([], {day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'}) : '';
          this.statusText = '●●●●●●●●  set ' + when + (resp.updatedby != null ? ' by ' + resp.updatedby : '');
        } else if(val != null && val != '' && val != RbPasswordInputComponent.MARKER) {
          this.statusText = '●●●●●●●●  plain text, moves to the secrets on next use';
        } else {
          this.statusText = '';
        }
      },
      error: () => { if(this.statusKey == key) this.statusText = 'status not available'; }
    });
  }

  public initEditedValue() {
    this.editedValue = '';
  }

  public startEditing() {
    super.startEditing();
    if(this.isEditing) this.inputType = 'password';
  }

  public finishEditing() {
    if(this.hadUserEdit && this.editedValue != null && this.editedValue != '') {
      this.putSecret(this.editedValue);
    }
    this.inputType = 'text';
    super.finishEditing();
  }

  public cancelEditing() {
    this.inputType = 'text';
    super.cancelEditing();
  }

  public clearValue() {
    this.putSecret(null);
  }

  private putSecret(value: string) {
    let name = this.secretName;
    let obj = this.rbObject;
    if(name == null || obj == null) return;
    this.apiService.secretPut(obj.domain, name, value).subscribe({
      next: () => {
        this.statusKey = null;
        this.commit(value != null ? RbPasswordInputComponent.MARKER : null);
      },
      error: (err) => this.dialogService.openDialog("The secret could not be saved: " + (err.message || err), [{label: "Ok", focus: true, callback: () => {}}])
    });
  }

  // The value is never shown, so a changed persisted value must not reselect the box while typing
  checkPersistedValueChangedWhileEditing() {
  }
}
