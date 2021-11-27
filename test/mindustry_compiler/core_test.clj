(ns mindustry-compiler.core-test
  (:require
   [clojure.test :as t]
   [mindustry-compiler.core :as sut]
   [instaparse.core :as insta]))

(def t-nil [:symbol "nil"])

(def insta-success? (complement insta/failure?))
(defn unterminated-string-error?
  [insta-error]
  (->> insta-error
       :reason
       (some #{{:tag :string :expecting "\""}})))


(t/deftest parse-int-test
  (t/testing "test parsing an int immediate"
           (t/is (= [:S [:number "7"]] (sut/parse "7")))))

(t/deftest parse-int-whitespace-before-test
  (t/testing "test parsing an int preceeded by whitespace"
           (t/is (= [:S [:number "7"]] (sut/parse "  7")))))

(t/deftest parse-int-whitespace-test
  (t/testing "test parsing an int surrounded by whitespace"
           (t/is (= [:S [:number "7"]] (sut/parse "  7 ")))))

(t/deftest parse-int-trailing-newline-test
  (t/testing "test parsing with an int and a newline only"
           (t/is (= [:S [:number "7"]] (sut/parse "7\n")))))

(t/deftest parse-nil-keword-test
  ;; Handle nil as symbol in parsing, resolve later
  (t/testing "test parsing nil as nil" (t/is (= [:S t-nil] (sut/parse "nil")))))

(t/deftest parse-empty-seq-test
  (t/testing "test parsing empty seq" (t/is (= [:S [:seq]] (sut/parse "()")))))

(t/deftest parse-1-seq-test (t/is (= [:S [:seq t-nil]] (sut/parse "(nil)"))))

(t/deftest parse-2-seq-test
  (t/is (= [:S [:seq t-nil t-nil]] (sut/parse "(nil nil)"))))

(t/deftest parse-nested-seq-no-space-test
  (t/is (= [:S [:seq t-nil [:seq t-nil]]] (sut/parse "(nil(nil))"))))

(t/deftest parse-nested-seq-space-test
  (t/is (= [:S [:seq t-nil [:seq t-nil]]] (sut/parse "(nil (nil))"))))

(t/deftest parse-symbol-test (t/is (= [:S [:symbol "test"]] (sut/parse "test"))))

(t/deftest parse-deref-test
  (t/is (= [:S [:deref [:symbol "test"]]] (sut/parse "@test"))))

(t/deftest parse-deref-space-test
  (t/is (= [:S [:deref [:symbol "test"]]] (sut/parse "@ test"))))

(t/deftest parse-string-test
  ;; Has to be individual strings since escape seqs are also possible
  (t/is (= [:S [:string "t" "e" "s" "t"]] (sut/parse "\"test\""))))

(t/deftest parse-string-esc-unprocessed-test
  (t/is (= [:S [:string "t" "e" [:escape-sequence "\\"] "s" "t"]]
         (sut/parse "\"te\\\\st\""))))

(t/deftest parse-string-esc-quote-test
  (t/is (= [:S
          [:string
           "t"
           [:escape-sequence "\""]
           "e"
           "s"
           [:escape-sequence "\""]
           "t"]]
         (sut/parse "\"t\\\"es\\\"t\""))))

(t/deftest parse-string-esc-quote-unbalanced-test
  (t/is (= [:S [:string "t" [:escape-sequence "\""] "e" "s" "t"]]
         (sut/parse "\"t\\\"est\""))))

(t/deftest parse-string-esc-whitespace-quote-unbalanced-test
  (t/testing
    "escaping whitespace before an invalid quote doesn't escape the quote"
    (t/is (unterminated-string-error? (sut/parse "\"test\\ \"\"")))))

(let [string-without-closing-quote "\"test\\\""]
  (t/deftest parse-string-esc-whitespace-quote-unterminated-test
    (t/testing "escaping a quote and then not terminating the string t/is an error"
             (t/is (insta-success? (sut/parse (str string-without-closing-quote
                                                 \"))))
             (t/is (unterminated-string-error?
                   (sut/parse string-without-closing-quote))))))
