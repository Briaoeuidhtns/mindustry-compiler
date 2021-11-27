(ns mindustry-compiler.core
  (:gen-class)
  (:require
   [instaparse.core :as insta]
   [clojure.java.io :as io]))

(insta/defparser parse (io/resource "grammar.bnf"))
