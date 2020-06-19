<% 
var showExpr = true;
if(typeof dataset != 'undefined') {
	showExpr = (config.show != null ? config.show : 'true').split('object').join(dataset + '.selectedObject').split('relatedObject').join(dataset + '.relatedObject');
	if(showExpr.indexOf('.selectedObject.') > -1) showExpr = dataset + '.selectedObject != null && (' + showExpr + ')';
	if(showExpr.indexOf('.relatedObject.') > -1) showExpr = dataset + '.relatedObject != null && (' + showExpr + ')';
}
%><div
	class="rb-hsection" 
	style="<%=config.inlineStyle%>"
	*ngIf="<%=showExpr%>">
	#content#
</div>
