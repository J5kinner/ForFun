import React, { useState } from "react";
import singleColumnImg from "../assets/images/singleCol.webp";
import doubleColumnImg from "../assets/images/doubleCol.webp";
import Preview from "./Preview";
import "../assets/css/style.css";

function FileUploader() {
  let htmlData;
  const defaultFileType = "html";

  const [selectFile, setFile] = useState({
    fileType: defaultFileType,
    fileDownloaderURL: null,
    status: "",
    newFileName: "",
    data: "",
  });

  const showFile = async (e) => {
    e.preventDefault();

    const reader = new FileReader();
    reader.onload = async (e) => {
      const textSave = e.target.result;
      htmlData = textSave;
      // console.log(htmlData);

      htmlData = textSave.replace(
        /(<style[\w\W]+style>)/g,
        `    <style>
        @import url("https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700;900&display=swap");
        @import url("https://fonts.googleapis.com/css2?family=Open+Sans&family=Roboto:wght@400;500;700;900&display=swap");
  
        @media only print {
           div.battlescribe div.summary {
            width: 100%;
            max-width: 650px;
            margin-left: auto;
            margin-right: auto;
          }
  
          div.battlescribe li.force {
            margin-right: auto;
            margin-left: auto;
            width: 100%;
            max-width: 650px;
          }
  
          div.battlescribe {
            font-size: 12px;
          }
  
          div.battlescribe h1 {
            font-size: 28px;
          }
  
          div.battlescribe p {
            font-size: 12px;
          }
  
          div.battlescribe table {
            font-size: 12px;
          }
  
          div.battlescribe h4 {
            font-size: 16px;
          }
  
          div.battlescribe li.rootselection {
            margin-bottom: 0px;
          }
  
          div.battlescribe h2 {
            font-size: 25px;
          }
  
          div.battlescribe h1 {
            width: 100%;
            max-width: 650px;
            margin-left: auto;
            margin-right: auto;
          }
  
          th {
            padding: 4px;
          }
  
          td {
            padding: 4px;
          }
  
          th:first-of-type {
            padding-left: 16px;
          }
  
          th:last-of-type {
            padding-right: 16px;
          }
  
          td:first-of-type {
            padding-left: 16px;
          }
  
          td:last-of-type {
            padding-right: 16px;
          }
        }
  
        li.force>p.rule-names {
          margin-bottom: 10px;
          margin-left: 16px;
          margin-top: -12px;
          font-variant: small-caps;
          font-size: 16px;
        }
  
        body.battlescribe {
          margin: 0px;
          padding: 0px;
          border-width: 0px;
        }
  
        div.battlescribe {
          margin-top: 0px;
          margin-bottom: 0px;
          margin-left: auto;
          margin-right: auto;
          padding: 8px;
          border-width: 0px;
          font-family: "Roboto", serif;
          color: #444444;
          text-align: left;
        }
  
        div.battlescribe h2,
        div.battlescribe h3,
        div.battlescribe h4 {
          margin: 0px;
          padding: 0px;
          border-width: 0px;
          line-height: 1em;
        }
  
        div.battlescribe h1 {
          margin-top: 8px;
          font-family: "Roboto", sans-serif;
          color: #222e33;
          text-align: left;
          text-transform: uppercase;
          line-height: 1.1em;
        }
  
        div.battlescribe h2 {
          font-family: "Roboto", sans-serif;
          color: #222e33;
          text-transform: uppercase;
          margin-left: auto;
          margin-right: auto;
          margin-bottom: 20px;
          padding-top: 10px;
          margin-top: 30px;
          border-top: 3px #e1ecf0 solid;
          line-height: 1.1em;
        }
  
        div.battlescribe h3 {
          font-size: 20px;
          font-family: "Roboto", sans-serif;
          color: #222e33;
          text-transform: uppercase;
          text-align: left;
          margin-bottom: 5px;
        }
  
        li.rootselection>h4 {
          font-family: "Roboto", sans-serif;
          font-size: 16px;
          padding-bottom: 5px;
          padding-left: 5px;
          color: #222e33;
          text-transform: uppercase;
        }
  
        div.battlescribe h1+p,
        .battlescribe>p:first-of-type {
          width: 50%;
          max-width: 400px;
          min-width: 200px;
          margin-right: auto;
          margin-left: auto;
          margin-bottom: 20px;
          font-style: italic;
          border-width: 1px;
          border-style: solid;
          padding: 10px;
          border-color: #e3510e;
        }
  
        div.battlescribe h1+p::before,
        .battlescribe>p:first-of-type::before {
          content: "Roster notes: ";
          font-weight: bold;
          font-style: normal;
        }
  
        div.summary h2 {
          margin-top: 20px;
          margin-bottom: 10px;
          padding: 0px;
          border-width: 0px;
          font-family: "Open Sans", sans-serif;
          font-size: 20px;
          text-align: left;
          padding-top: 10px;
          border-top: 3px #e1ecf0 solid;
        }
  
        li.rootselection>ul>li>h4 {
          font-variant: small-caps;
          padding-left: 5px;
        }
  
        div.battlescribe div.summary {
          page-break-before: avoid;
          page-break-inside: avoid;
        }
  
        div.battlescribe div.summary p {
          padding: 10px;
          border: 1px #222e33 solid;
          margin-left: 0px;
        }
  
        div.battlescribe ul {
          margin: 0px 0px -5px 0px;
          padding: 0px;
          border-width: 0px;
          list-style-image: none;
          list-style-position: outside;
          list-style-type: none;
        }
  
        div.battlescribe li {
          margin: 8px 0px 0px 0px;
          padding: 0px;
          border-width: 0px;
        }
  
        div.battlescribe li.force {
          margin-bottom: 16px;
          padding: 0px;
          border-width: 0px;
        }
  
        div.battlescribe li.category {
          margin: 0px 0px 16px 0px;
          padding: 0px;
          border-width: 0px;
          break-before: page;
        }
  
        div.battlescribe li.rootselection {
          padding-top: 5px;
          border-width: 1px;
          border-style: solid;
          border-color: #222e33;
          page-break-inside: avoid;
          line-height: 1.1em;
        }
  
        div.battlescribe p {
          margin: 4px 0px 0px 16px;
          padding: 0px;
          border-width: 0px;
        }
  
        div.battlescribe p.category-names {
          margin-bottom: 5px;
          margin-right: 2px;
        }
  
        div.battlescribe p.profile-names {
          display: none;
        }
  
        div.battlescribe table:first-of-type {
          margin-top: -10px;
        }
  
        li.category>ul>li.rootselection>ul,
        li.rootselection>p.rule-names {
          margin-bottom: 4px;
        }
  
        div.battlescribe table {
          margin-bottom: 0px;
          margin-right: auto;
          margin-left: auto;
          padding: 0px;
          border-collapse: collapse;
          width: 100%;
          color: #444444;
          page-break-inside: avoid;
        }
  
        div.battlescribe tr {
          border-width: 0px;
          border-style: solid;
          border-color: #222e33;
        }
  
        tr:nth-child(even) {
          background-color: #e1ecf0;
        }
  
        tr:nth-child(odd) {
          background: #fff;
        }
  
        div.battlescribe th {
          margin: 0px;
          border-width: 0px;
          color: white;
          font-weight: bold;
          text-align: left;
          background-color: #222e33;
        }
  
        div.battlescribe td {
          margin: 0px;
          border-width: 0px;
          text-align: left;
          line-height: 1.1em;
          vertical-align: top;
        }
  
        div.battlescribe td.profile-name {
          font-weight: bold;
        }
  
        div.battlescribe td.statistic-name {
          font-weight: bold;
        }
  
        div.battlescribe table.statistics tr.subtotal {
          font-weight: bold;
        }
  
        div.battlescribe table.statistics tr.total {
          font-size: 13px;
          font-weight: bold;
        }
  
        div.battlescribe table.statistics th {
          border-width: 1px;
          border-style: solid;
          border-color: #bbbbbb;
          font-size: 13px;
          text-align: right;
        }
  
        div.battlescribe table.statistics th.center {
          text-align: center;
        }
  
        div.battlescribe table.statistics td {
          border-width: 4px;
          border-style: solid;
          border-color: #bbbbbb;
          text-align: right;
        }
  
        div.battlescribe span.bold {
          font-weight: bold;
        }
  
        div.battlescribe span.italic {
          font-style: italic;
        }
  
        div.battlescribe span.caps {
          font-variant: small-caps;
          font-weight: bold;
        }
  
        br+p {
          display: none;
        }
  
        }
  
        @media only screen {
          div.battlescribe div.summary {
            width: 100%;
            max-width: 650px;
            margin-left: auto;
            margin-right: auto;
          }
  
          div.battlescribe li.force {
            margin-right: auto;
            margin-left: auto;
            width: 100%;
            max-width: 650px;
          }
  
          div.battlescribe {
            font-size: 12px;
          }
  
          div.battlescribe h1 {
            font-size: 28px;
          }
  
          div.battlescribe p {
            font-size: 12px;
          }
  
          div.battlescribe table {
            font-size: 12px;
          }
  
          div.battlescribe h4 {
            font-size: 16px;
          }
  
          div.battlescribe li.rootselection {
            margin-bottom: 0px;
          }
  
          div.battlescribe h2 {
            font-size: 25px;
          }
  
          div.battlescribe h1 {
            width: 100%;
            max-width: 650px;
            margin-left: auto;
            margin-right: auto;
          }
  
          th {
            padding: 4px;
          }
  
          td {
            padding: 4px;
          }
  
          th:first-of-type {
            padding-left: 16px;
          }
  
          th:last-of-type {
            padding-right: 16px;
          }
  
          td:first-of-type {
            padding-left: 16px;
          }
  
          td:last-of-type {
            padding-right: 16px;
          }
        }
  
        li.force>p.rule-names {
          margin-bottom: 10px;
          margin-left: 16px;
          margin-top: -12px;
          font-variant: small-caps;
          font-size: 16px;
        }
  
        body.battlescribe {
          margin: 0px;
          padding: 0px;
          border-width: 0px;
        }
  
        div.battlescribe {
          margin-top: 0px;
          margin-bottom: 0px;
          margin-left: auto;
          margin-right: auto;
          padding: 8px;
          border-width: 0px;
          font-family: "Roboto", serif;
          color: #444444;
          text-align: left;
        }
  
        div.battlescribe h2,
        div.battlescribe h3,
        div.battlescribe h4 {
          margin: 0px;
          padding: 0px;
          border-width: 0px;
          line-height: 1em;
        }
  
        div.battlescribe h1 {
          margin-top: 8px;
          font-family: "Roboto", sans-serif;
          color: #222e33;
          text-align: left;
          text-transform: uppercase;
          line-height: 1.1em;
        }
  
        div.battlescribe h2 {
          font-family: "Roboto", sans-serif;
          color: #222e33;
          text-transform: uppercase;
          margin-left: auto;
          margin-right: auto;
          margin-bottom: 20px;
          padding-top: 10px;
          margin-top: 30px;
          border-top: 3px #e1ecf0 solid;
          line-height: 1.1em;
        }
  
        div.battlescribe h3 {
          font-size: 20px;
          font-family: "Roboto", sans-serif;
          color: #222e33;
          text-transform: uppercase;
          text-align: left;
          margin-bottom: 5px;
        }
  
        li.rootselection>h4 {
          font-family: "Roboto", sans-serif;
          font-size: 16px;
          padding-bottom: 5px;
          padding-left: 5px;
          color: #222e33;
          text-transform: uppercase;
        }
  
        div.battlescribe h1+p,
        .battlescribe>p:first-of-type {
          width: 50%;
          max-width: 400px;
          min-width: 200px;
          margin-right: auto;
          margin-left: auto;
          margin-bottom: 20px;
          font-style: italic;
          border-width: 1px;
          border-style: solid;
          padding: 10px;
          border-color: #e3510e;
        }
  
        div.battlescribe h1+p::before,
        .battlescribe>p:first-of-type::before {
          content: "Roster notes: ";
          font-weight: bold;
          font-style: normal;
        }
  
        div.summary h2 {
          margin-top: 20px;
          margin-bottom: 10px;
          padding: 0px;
          border-width: 0px;
          font-family: "Open Sans", sans-serif;
          font-size: 20px;
          text-align: left;
          padding-top: 10px;
          border-top: 3px #e1ecf0 solid;
        }
  
        li.rootselection>ul>li>h4 {
          font-variant: small-caps;
          padding-left: 5px;
        }
  
        div.battlescribe div.summary {
          page-break-before: avoid;
          page-break-inside: avoid;
        }
  
        div.battlescribe div.summary p {
          padding: 10px;
          border: 1px #222e33 solid;
          margin-left: 0px;
        }
  
        div.battlescribe ul {
          margin: 0px 0px -5px 0px;
          padding: 0px;
          border-width: 0px;
          list-style-image: none;
          list-style-position: outside;
          list-style-type: none;
        }
  
        div.battlescribe li {
          margin: 8px 0px 0px 0px;
          padding: 0px;
          border-width: 0px;
        }
  
        div.battlescribe li.force {
          margin-bottom: 16px;
          padding: 0px;
          border-width: 0px;
        }
  
        div.battlescribe li.category {
          margin: 0px 0px 16px 0px;
          padding: 0px;
          border-width: 0px;
          break-before: page;
        }
  
        div.battlescribe li.rootselection {
          padding-top: 5px;
          border-width: 1px;
          border-style: solid;
          border-color: #222e33;
          page-break-inside: avoid;
          line-height: 1.1em;
        }
  
        div.battlescribe p {
          margin: 4px 0px 0px 16px;
          padding: 0px;
          border-width: 0px;
        }
  
        div.battlescribe p.category-names {
          margin-bottom: 5px;
          margin-right: 2px;
        }
  
        div.battlescribe p.profile-names {
          display: none;
        }
  
        div.battlescribe table:first-of-type {
          margin-top: -10px;
        }
  
        li.category>ul>li.rootselection>ul,
        li.rootselection>p.rule-names {
          margin-bottom: 4px;
        }
  
        div.battlescribe table {
          margin-bottom: 0px;
          margin-right: auto;
          margin-left: auto;
          padding: 0px;
          border-collapse: collapse;
          width: 100%;
          color: #444444;
          page-break-inside: avoid;
        }
  
        div.battlescribe tr {
          border-width: 0px;
          border-style: solid;
          border-color: #222e33;
        }
  
        tr:nth-child(even) {
          background-color: #e1ecf0;
        }
  
        tr:nth-child(odd) {
          background: #fff;
        }
  
        div.battlescribe th {
          margin: 0px;
          border-width: 0px;
          color: white;
          font-weight: bold;
          text-align: left;
          background-color: #222e33;
        }
  
        div.battlescribe td {
          margin: 0px;
          border-width: 0px;
          text-align: left;
          line-height: 1.1em;
          vertical-align: top;
        }
  
        div.battlescribe td.profile-name {
          font-weight: bold;
        }
  
        div.battlescribe td.statistic-name {
          font-weight: bold;
        }
  
        div.battlescribe table.statistics tr.subtotal {
          font-weight: bold;
        }
  
        div.battlescribe table.statistics tr.total {
          font-size: 13px;
          font-weight: bold;
        }
  
        div.battlescribe table.statistics th {
          border-width: 1px;
          border-style: solid;
          border-color: #bbbbbb;
          font-size: 13px;
          text-align: right;
        }
  
        div.battlescribe table.statistics th.center {
          text-align: center;
        }
  
        div.battlescribe table.statistics td {
          border-width: 4px;
          border-style: solid;
          border-color: #bbbbbb;
          text-align: right;
        }
  
        div.battlescribe span.bold {
          font-weight: bold;
        }
  
        div.battlescribe span.italic {
          font-style: italic;
        }
  
        div.battlescribe span.caps {
          font-variant: small-caps;
          font-weight: bold;
        }
  
        br+p {
          display: none;
        }
      </style>`
      );
      setFile({ data: htmlData });
    };
    reader.readAsText(e.target.files[0]);
  };

  const show2ColFile = async (e) => {
    e.preventDefault();
    const reader = new FileReader();
    reader.onload = async (e) => {
      const textSave = e.target.result;
      htmlData = textSave;
      htmlData = textSave.replace(
        /(<style[\w\W]+style>)/g,
        `    <style>
        @import url("https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700;900&display=swap");
        @import url("https://fonts.googleapis.com/css2?family=Open+Sans&family=Roboto:wght@400;500;700;900&display=swap");
  
        @media only print {
          div.battlescribe {
            columns: 200px 2;
          }
  
          li.rootselection > h4 {
            font-size: 12px;
          }
  
          div.battlescribe {
            font-size: 10px;
          }
  
          div.battlescribe p {
            font-size: 10px;
          }
  
          div.battlescribe table {
            font-size: 10px;
          }
  
          div.battlescribe h4 {
            font-size: 13px;
          }
  
          div.battlescribe li.force {
            margin: 0px 10px 6px 0px;
          }
  
          div.battlescribe h1 {
            font-size: 24px;
          }
  
          div.battlescribe h2 {
            font-size: 20px;
          }
  
          div.battlescribe div.summary {
            page-break-before: avoid;
            page-break-inside: avoid;
            margin-right: 10px;
            float: left;
          }
  
          th {
            padding: 4px;
          }
  
          td {
            padding: 4px;
          }
        }
  
        @media only screen {
          div.battlescribe div.summary {
            width: 100%;
            max-width: 650px;
            margin-left: auto;
            margin-right: auto;
          }
  
          div.battlescribe li.force {
            margin-right: auto;
            margin-left: auto;
            width: 100%;
            max-width: 650px;
          }
  
          div.battlescribe {
            columns: 200px 2;
  
            font-size: 12px;
          }
  
          div.battlescribe h1 {
            font-size: 28px;
          }
  
          div.battlescribe p {
            font-size: 12px;
          }
  
          div.battlescribe table {
            font-size: 12px;
          }
  
          div.battlescribe h4 {
            font-size: 16px;
          }
  
          div.battlescribe li.rootselection {
            margin-bottom: 0px;
          }
  
          div.battlescribe h2 {
            font-size: 25px;
          }
  
          div.battlescribe h1 {
            width: 100%;
            max-width: 650px;
            margin-left: auto;
            margin-right: auto;
          }
  
          th {
            padding: 4px;
          }
  
          td {
            padding: 4px;
          }
  
          th:first-of-type {
            padding-left: 16px;
          }
  
          th:last-of-type {
            padding-right: 16px;
          }
  
          td:first-of-type {
            padding-left: 16px;
          }
  
          td:last-of-type {
            padding-right: 16px;
          }
        }
  
        li.force > p.rule-names {
          margin-bottom: 10px;
          margin-left: 16px;
          margin-top: -12px;
          font-variant: small-caps;
          font-size: 16px;
        }
  
        body.battlescribe {
          margin: 0px;
          padding: 0px;
          border-width: 0px;
        }
  
        div.battlescribe {
          margin-top: 0px;
          margin-bottom: 0px;
          margin-left: auto;
          margin-right: auto;
          padding: 8px;
          border-width: 0px;
          font-family: "Roboto", serif;
          color: #444444;
          text-align: left;
        }
  
        div.battlescribe h2,
        div.battlescribe h3,
        div.battlescribe h4 {
          margin: 0px;
          padding: 0px;
          border-width: 0px;
          line-height: 1em;
        }
  
        div.battlescribe h1 {
          margin-top: 8px;
          font-family: "Roboto", sans-serif;
          color: #222e33;
          text-align: left;
          text-transform: uppercase;
          line-height: 1.1em;
        }
  
        div.battlescribe h2 {
          font-family: "Roboto", sans-serif;
          color: #222e33;
          text-transform: uppercase;
          margin-left: auto;
          margin-right: auto;
          margin-bottom: 20px;
          padding-top: 10px;
          margin-top: 30px;
          border-top: 3px #e1ecf0 solid;
          line-height: 1.1em;
        }
  
        div.battlescribe h3 {
          font-size: 20px;
          font-family: "Roboto", sans-serif;
          color: #222e33;
          text-transform: uppercase;
          text-align: left;
          margin-bottom: 5px;
        }
  
        li.rootselection > h4 {
          font-family: "Roboto", sans-serif;
          font-size: 16px;
          padding-bottom: 5px;
          padding-left: 5px;
          color: #222e33;
          text-transform: uppercase;
        }
  
        div.battlescribe h1 + p,
        .battlescribe > p:first-of-type {
          width: 50%;
          max-width: 400px;
          min-width: 200px;
          margin-right: auto;
          margin-left: auto;
          margin-bottom: 20px;
          font-style: italic;
          border-width: 1px;
          border-style: solid;
          padding: 10px;
          border-color: #e3510e;
        }
  
        div.battlescribe h1 + p::before,
        .battlescribe > p:first-of-type::before {
          content: "Roster notes: ";
          font-weight: bold;
          font-style: normal;
        }
  
        div.summary h2 {
          margin-top: 20px;
          margin-bottom: 10px;
          padding: 0px;
          border-width: 0px;
          font-family: "Open Sans", sans-serif;
          font-size: 20px;
          text-align: left;
          padding-top: 10px;
          border-top: 3px #e1ecf0 solid;
        }
  
        li.rootselection > ul > li > h4 {
          font-variant: small-caps;
          padding-left: 5px;
        }
  
        div.battlescribe div.summary {
          page-break-before: avoid;
          page-break-inside: avoid;
        }
  
        div.battlescribe div.summary p {
          padding: 10px;
          border: 1px #222e33 solid;
          margin-left: 0px;
        }
  
        div.battlescribe ul {
          margin: 0px 0px -5px 0px;
          padding: 0px;
          border-width: 0px;
          list-style-image: none;
          list-style-position: outside;
          list-style-type: none;
        }
  
        div.battlescribe li {
          margin: 8px 0px 0px 0px;
          padding: 0px;
          border-width: 0px;
        }
  
        div.battlescribe li.force {
          margin-bottom: 16px;
          padding: 0px;
          border-width: 0px;
        }
  
        div.battlescribe li.category {
          margin: 0px 0px 16px 0px;
          padding: 0px;
          border-width: 0px;
          break-before: page;
        }
  
        div.battlescribe li.rootselection {
          padding-top: 5px;
          border-width: 1px;
          border-style: solid;
          border-color: #222e33;
          page-break-inside: avoid;
          line-height: 1.1em;
        }
  
        div.battlescribe p {
          margin: 4px 0px 0px 16px;
          padding: 0px;
          border-width: 0px;
        }
  
        div.battlescribe p.category-names {
          margin-bottom: 5px;
          margin-right: 2px;
        }
  
        div.battlescribe p.rule-names {
        }
  
        div.battlescribe p.profile-names {
          display: none;
        }
  
        div.battlescribe span.bold {
        }
  
        div.battlescribe table:first-of-type {
          margin-top: -10px;
        }
  
        li.category > ul > li.rootselection > ul,
        li.rootselection > p.rule-names {
          margin-bottom: 4px;
        }
  
        div.battlescribe table {
          margin-bottom: 0px;
          margin-right: auto;
          margin-left: auto;
          padding: 0px;
          border-collapse: collapse;
          width: 100%;
          color: #444444;
          page-break-inside: avoid;
        }
  
        div.battlescribe tr {
          border-width: 0px;
          border-style: solid;
          border-color: #222e33;
        }
  
        tr:nth-child(even) {
          background-color: #e1ecf0;
        }
  
        tr:nth-child(odd) {
          background: #fff;
        }
  
        div.battlescribe th {
          margin: 0px;
          border-width: 0px;
          color: white;
          font-weight: bold;
          text-align: left;
          background-color: #222e33;
        }
  
        div.battlescribe td {
          margin: 0px;
          border-width: 0px;
          text-align: left;
          line-height: 1.1em;
          vertical-align: top;
        }
  
        div.battlescribe td.profile-name {
          font-weight: bold;
        }
  
        div.battlescribe td.statistic-name {
          font-weight: bold;
        }
  
        div.battlescribe table.statistics {
        }
  
        div.battlescribe table.statistics tr.subtotal {
          font-weight: bold;
        }
  
        div.battlescribe table.statistics tr.total {
          font-size: 13px;
          font-weight: bold;
        }
  
        div.battlescribe table.statistics th {
          border-width: 1px;
          border-style: solid;
          border-color: #bbbbbb;
          font-size: 13px;
          text-align: right;
        }
  
        div.battlescribe table.statistics th.center {
          text-align: center;
        }
  
        div.battlescribe table.statistics td {
          border-width: 4px;
          border-style: solid;
          border-color: #bbbbbb;
          text-align: right;
        }
  
        div.battlescribe span.bold {
          font-weight: bold;
        }
  
        div.battlescribe span.italic {
          font-style: italic;
        }
  
        div.battlescribe span.caps {
          font-variant: small-caps;
          font-weight: bold;
        }
  
        br + p {
          display: none;
        }
  
        @media only print {
          div.battlescribe {
            columns: 200px 2;
          }
  
          li.rootselection > h4 {
            font-size: 12px;
          }
  
          div.battlescribe {
            font-size: 10px;
          }
  
          div.battlescribe p {
            font-size: 10px;
          }
  
          div.battlescribe table {
            font-size: 10px;
          }
  
          div.battlescribe h4 {
            font-size: 13px;
          }
  
          div.battlescribe li.force {
            margin: 0px 10px 6px 0px;
          }
  
          div.battlescribe h1 {
            font-size: 24px;
          }
  
          div.battlescribe h2 {
            font-size: 20px;
          }
  
          div.battlescribe div.summary {
            page-break-before: avoid;
            page-break-inside: avoid;
            margin-right: 10px;
            float: left;
          }
  
          th {
            padding: 4px;
          }
  
          td {
            padding: 4px;
          }
        }
      </style>`
      );
      setFile({ data: htmlData });
    };

    reader.readAsText(e.target.files[0]);
  };

  const download = async (e) => {
    e.preventDefault();
    const blob = new Blob([selectFile.data], { type: "text/html" });
    const fileDownloadUrl = URL.createObjectURL(blob);
    console.log(blob);
    // console.log(selectFile.newFileName)
    setFile(
      { fileDownloaderURL: fileDownloadUrl, newFileName: "40kBeautified.html" },

      console.log("Download file " + selectFile.newFileName),
      () => {
        selectFile.dofileDownload.click();
        URL.revokeObjectURL(fileDownloadUrl); // free up storage--no longer needed.
        setFile({ fileDownloadUrl: "" });
      }
    );
  };

  const download2Col = async (e) => {
    e.preventDefault();
    const blob = new Blob([selectFile.data], { type: "text/html" });
    const fileDownloadUrl = URL.createObjectURL(blob);
    console.log(blob);
    // console.log(selectFile.newFileName)
    setFile(
      {
        fileDownloaderURL: fileDownloadUrl,
        newFileName: "40kBeautified2Col.html",
      },

      console.log("Download file " + selectFile.newFileName),
      () => {
        selectFile.dofileDownload.click();
        URL.revokeObjectURL(fileDownloadUrl); // free up storage--no longer needed.
        setFile({ fileDownloadUrl: "" });
      }
    );
  };

  return (
    <div className="upload-container">
      <div className="upload-type">
        <Preview title="Single Column" image={singleColumnImg} />
        <input type="file" name="file" onChange={showFile} />
        {selectFile.newFileName === "40kBeautified.html" ? (
          <a
            className="hidden"
            download={selectFile.newFileName}
            href={selectFile.fileDownloaderURL}
            ref={selectFile}
          >
            Download it
          </a>
        ) : (
          <button onClick={download}>Transform</button>
        )}
      </div>
      <br />
      <div className="upload-type">
        <Preview title="Double Column" image={doubleColumnImg} />
        <input type="file" name="file" onChange={show2ColFile} />
        {selectFile.newFileName === "40kBeautified2Col.html" ? (
          <a
            className="hidden"
            download={selectFile.newFileName}
            href={selectFile.fileDownloaderURL}
            ref={selectFile}
          >
            Download it
          </a>
        ) : (
          <button onClick={download2Col}>Transform</button>
        )}
      </div>
    </div>
  );
}

export default FileUploader;
