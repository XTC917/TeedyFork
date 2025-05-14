'use strict';

/**
 * Document modal PDF controller.
 */
angular.module('docs').controller('DocumentModalPdf', function ($scope, $window, $stateParams, $uibModalInstance) {
  $scope.export = {
    metadata: false,
    comments: false,
    fitimagetopage: true,
    margin: 10,
    translate: false,
    targetLanguage: 'zh'
  };

  // Export to PDF
  $scope.exportPdf = function() {
    var url = '../api/document/' + $stateParams.id
        + '/pdf?metadata=' + $scope.export.metadata
        + '&comments=' + $scope.export.comments
        + '&fitimagetopage=' + $scope.export.fitimagetopage
        + '&margin=' + $scope.export.margin;
    
    if ($scope.export.translate) {
      url += '&translate=true&target_language=' + $scope.export.targetLanguage;
    }
    
    $window.open(url);
    $uibModalInstance.close();
  };

  // Close the modal
  $scope.close = function () {
    $uibModalInstance.close();
  }
});