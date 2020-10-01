<rb-modal
	#<%=id%>="modal"
	[name]="<%=config.name%>"
	[hidden]="activeModal != '<%=config.name%>'"
	(close)="closeModal()">
	#content#
</rb-modal>
