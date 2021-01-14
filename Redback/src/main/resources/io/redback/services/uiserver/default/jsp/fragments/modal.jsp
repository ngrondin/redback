<rb-modal
	#<%=id%>="modal"
	[name]="<%=config.name%>"
	(closeModal)="closeModal()">
	#content#
</rb-modal>
