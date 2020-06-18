<% 
var showExpr = true;
if(typeof dataset != 'undefined') {
	showExpr = (config.show != null ? config.show : 'true').replace(/object/g, dataset + '.selectedObject').replace(/relatedObject/g, dataset + '.relatedObject');
	if(showExpr.indexOf('.selectedObject.') > -1) showExpr = dataset + '.selectedObject != null && (' + showExpr + ')';
	if(showExpr.indexOf('.relatedObject.') > -1) showExpr = dataset + '.relatedObject != null && (' + showExpr + ')';
}
%><div
	class="rb-vsection" 
	style="<%=config.inlineStyle%>"
	*ngIf="<%=showExpr%>">
	#content#
</div>
