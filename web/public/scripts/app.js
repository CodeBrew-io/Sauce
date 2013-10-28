'use strict';

function hereDoc(f) {
        return f.toString().
          replace(/^[^\/]+\/\*!?/, '').
          replace(/\*\/[^\/]+$/, '');
}

var app = angular.module('protoApp', ['ui.codemirror']);