import {Component, Inject, OnInit} from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  FormGroupDirective,
  NgForm,
  ValidationErrors,
  ValidatorFn,
  Validators
} from "@angular/forms";
import {UserService} from "../../../../assets/service/user.service";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {DialogService} from "../../../../assets/service/dialog.service";
import {ErrorStateMatcher} from "@angular/material/core";
import {SuccessDialogComponent} from "../../../dialog/success-dialog/success-dialog.component";

export class PasswordErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    const invalidCtrl = !!(control?.invalid && control?.parent?.dirty);
    const invalidParent = !!(control?.parent?.invalid && control?.parent?.dirty);

    return (invalidCtrl || invalidParent) && control.touched;
  }
}

@Component({
  selector: 'app-dialog-user-change-password',
  templateUrl: './dialog-user-change-password.component.html',
  styleUrls: ['./dialog-user-change-password.component.scss']
})
export class DialogUserChangePasswordComponent implements OnInit {

  matcher = new PasswordErrorStateMatcher();

  form = new FormGroup({
    oldPassword: new FormControl('', [Validators.required]),
    newPassword: new FormControl('', [Validators.required, Validators.pattern("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,64}")]),
    confirmPassword: new FormControl('', [Validators.required]),
  }, {validators: this.checkPasswordsValidator()});

  exiting: boolean = false;

  constructor(public dialogRef: MatDialogRef<DialogUserChangePasswordComponent>,
              @Inject(MAT_DIALOG_DATA) public data,
              private dialog: MatDialog,
              private userService: UserService,
              private errorHandleService: DialogService) {
    dialogRef.disableClose = true;
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.dialog.open(SuccessDialogComponent, {
          data: {
            description: "Hasło zostało pomyślnie zmienione"
          }
        });
      }
    });
  }

  ngOnInit(): void {
  }

  private checkPasswordsValidator(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      let pass = group.get('newPassword')?.value;
      let confirmPass = group.get('confirmPassword')?.value
      return pass === confirmPass ? null : {notSame: true}
    };
  }

  changePassword(oldValue: string | null, newValue: string | null) {
    if (this.data.user && oldValue && newValue) {
      this.exiting = true;
      this.userService.modifyUserPasswordField(this.data.user.id, {
        oldPassword: oldValue,
        newPassword: newValue
      }).subscribe({
        next: () => {
          this.dialogRef.close(true);
        },
        error: err => {
          this.exiting = false;
          if (err.status === 400) {
            this.errorHandleService.openDataErrorDialog(err.message);
          }
        }
      })
    }
  }
}
