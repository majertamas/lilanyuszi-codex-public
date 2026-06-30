import {Component, inject, OnInit} from '@angular/core';
import {ListService} from './list-service';
import {Group, GroupRequest, GroupRole, GroupType} from '../../model/group.model';
import {ButtonDirective, ButtonModule} from 'primeng/button';
import {InputText} from 'primeng/inputtext';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {Ripple} from 'primeng/ripple';
import {TableModule} from 'primeng/table';
import {Tag} from 'primeng/tag';
import {ShoppingListService} from '../shopping-lists/shopping-list.service';
import {ModifyGroupName} from '../../service/modify-group-name.interface';
import {ConfirmationService, MessageService} from 'primeng/api';
import {ToastModule} from 'primeng/toast';
import {ConfirmDialog} from 'primeng/confirmdialog';
import {Dialog} from 'primeng/dialog';
import {Select} from 'primeng/select';

@Component({
  selector: 'app-lists',
  imports: [
    ButtonDirective,
    InputText,
    ReactiveFormsModule,
    Ripple,
    TableModule,
    Tag,
    FormsModule,
    ConfirmDialog,
    ToastModule,
    ButtonModule,
    Dialog,
    Select
  ],
  templateUrl: './lists.html',
  styleUrl: './lists.css',
})
export class Lists implements OnInit {

  private readonly listService: ListService = inject(ListService);
  private readonly shoppingListService: ShoppingListService = inject(ShoppingListService);
  private readonly messageService: MessageService = inject(MessageService);
  private readonly confirmationService: ConfirmationService = inject(ConfirmationService);
  protected lists: Group[] = [];
  private clonedLists: { [s: string]: Group } = {};
  protected dialogVisible: boolean = false;
  readonly form = new FormGroup({
    groupName: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    }),
    groupType: new FormControl<'SHOPPING' | 'RECIPE' | null>(
      null,
      Validators.required
    )
  });
  protected groupTypes: GroupType[] = ['SHOPPING', 'RECIPE'];

  ngOnInit() {
    this.getLists();
  }

  private getLists(): void {
    this.listService.getListsWhereMember().subscribe({
      next: (groups: Group[]): void => {
        this.lists = [...groups].sort((a, b) => a.name.localeCompare(b.name));
      }
    });
  }

  protected onRowEditInit(shoppingList: Group) {
    this.clonedLists[shoppingList.id] = {...shoppingList};
  }

  protected onRowEditSave(list: Group, index: number) {
    const service: ModifyGroupName | null = this.getServiceByType(list.type);
    if (!service) {
      this.messageService.add({severity: 'error', summary: 'Error', detail: 'Invalid list type'});
      this.onRowEditCancel(list, index);
      return;
    }

    service.modifyGroupName({id: list.id, newName: list.name})
      .subscribe({
        next: (group: Group): void => {
          if (group) {
            this.lists[index] = group;
            this.lists = [...this.lists].sort((a, b) => a.name.localeCompare(b.name));
            delete this.clonedLists[list.id];
          } else {
            this.onRowEditCancel(list, index);
          }
        }
      });
  }

  protected onRowEditCancel(shoppingList: Group, index: number) {
    this.lists[index] = this.clonedLists[shoppingList.id];
    delete this.clonedLists[shoppingList.id];
  }

  protected getSeverity(role: GroupRole) {
    switch (role) {
      case 'OWNER':
        return 'success';
      case 'ADMIN':
        return 'success';
      case 'MEMBER':
        return 'info';
    }
  }

  confirmDelete(event: Event, list: Group) {
    this.confirmationService.confirm({
      target: event.target as EventTarget,
      message: 'Do you want to delete this list?',
      header: 'Danger Zone',
      icon: 'pi pi-info-circle',
      rejectLabel: 'Cancel',
      rejectButtonProps: {
        label: 'Cancel',
        severity: 'secondary',
        outlined: true,
      },
      acceptButtonProps: {
        label: 'Delete',
        severity: 'danger',
      },

      accept: () => {
        this.listService.deleteList(list.id, list.type, list.isOwner).subscribe({
          next: () => {
            this.getLists();
          }
        });
      },
      reject: () => {
        this.messageService.add({severity: 'info', summary: 'Rejected', detail: 'You have rejected'});
      },
    });
  }

  private getServiceByType(type: GroupType): ModifyGroupName | null {
    switch (type) {
      case 'SHOPPING':
        return this.shoppingListService;
      case 'RECIPE':
        return null;
      default:
        return null;
    }
  }

  protected addNewList() {
    if (this.form.valid) {
      const request: GroupRequest = {name: this.form.get('groupName')!.value, type: this.form.get('groupType')!.value!};
      this.listService.addNewList(request).subscribe({
        next: (): void => {
          this.form.reset();
          this.dialogVisible = false;
          this.getLists();
        }
      });
    } else {
      this.messageService.add({severity: 'error', summary: 'Error', detail: 'Please fill in all required fields'});
    }
  }
}
