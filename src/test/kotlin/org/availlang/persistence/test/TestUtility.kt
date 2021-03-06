/*
 * TestUtility.kt
 * Copyright © 1993-2022, The Avail Foundation, LLC.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of the contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.availlang.persistence.test

import org.availlang.json.JSONFriendly
import org.availlang.json.JSONNumber
import org.availlang.json.JSONObject
import org.availlang.json.JSONReader
import org.availlang.json.JSONValue
import org.availlang.json.JSONWriter
import org.availlang.persistence.IndexedFile
import org.availlang.persistence.IndexedFileBuilder
import java.io.File
import java.io.StringReader
import java.lang.IllegalStateException

/**
 * A builder used to create new [IndexedFile]s that are meant to contain
 * [TestDocument]s.
 */
object TestIndexedFileBuilder: IndexedFileBuilder("Test Indexed File")
{
	fun create (backingFileName: String): IndexedFile =
		openOrCreate(
			fileReference = file(backingFileName),
			true)

	fun create (backingFileName: String, pageSize: Int): IndexedFile =
		openOrCreate(
			fileReference = file(backingFileName),
			forWriting = true,
			pageSize = pageSize)

	fun open (backingFileName: String): IndexedFile =
		openOrCreate(
			fileReference = file(backingFileName),
			forWriting = true)

	const val testDirectory = "test-output"

	private fun file(fileName: String): File =
		File("$testDirectory/$fileName")
}

data class TestSubDocument constructor(
	val tag: String,
	val allMyInts: List<Int>) : JSONFriendly
{
	override fun writeTo(writer: JSONWriter)
	{
		writer.at("tag") { write(tag) }
		writer.write("allMyInts")
		writer.writeArray { allMyInts.forEach { write(it) } }
	}

	companion object
	{
		/**
		 * Create a new [TestDocument] from a [JSONObject].
		 *
		 * @param json
		 *   The [JSONObject] that contains the state of the [TestDocument].
		 * @return
		 *   A `TestDocument`.
		 */
		fun from (json: JSONObject): TestSubDocument
		{
			val allMyInts = mutableListOf<Int>()
			json.getArray("allMyInts").forEach {
				if (!it.isNumber)
				{
					throw IllegalStateException(
						"Expected a number while deserializing a TestDocument")
				}
				allMyInts.add((it as JSONNumber).int)
			}
			return TestSubDocument(
				json.getString("tag"),
				allMyInts)
		}
	}
}

/**
 * A test object that can be serialized/deserialized into/from JSON.
 */
data class TestDocument constructor(
	val foo: Int,
	val bar: Double,
	val baz: String,
	val stuff: List<Boolean>,
	val allMySubs: List<TestSubDocument>): JSONFriendly
{
	override fun writeTo(writer: JSONWriter)
	{
		writer.at("foo") { write(foo) }
		writer.at("bar") { write(bar) }
		writer.at("baz") { write(baz) }
		writer.write("stuff")
		writer.writeArray { stuff.forEach { write(it) } }
		writer.write("allMySubs")
		writer.writeArray {
			allMySubs.forEach {
				writer.startObject()
				it.writeTo(writer)
				writer.endObject()
			}
		}
	}

	/**
	 * @return
	 *   The JSON object containing this [TestDocument] as a String.
	 */
	fun toJson (): String
	{
		val writer = JSONWriter()
		writer.startObject()
		writeTo(writer)
		writer.endObject()
		return writer.toString()
	}

	/**
	 * @return
	 *   The String JSON form of this [TestDocument] as a UTF-8 `ByteArray`.
	 */
	fun serialize (): ByteArray = toJson().encodeToByteArray()

	companion object
	{
		/**
		 * Create a new [TestDocument] from a [JSONObject].
		 *
		 * @param json
		 *   The [JSONObject] that contains the state of the [TestDocument].
		 * @return
		 *   A `TestDocument`.
		 */
		fun from (json: JSONObject): TestDocument
		{
			val stuff = mutableListOf<Boolean>()
			json.getArray("stuff").forEach {
				if (!it.isBoolean)
				{
					throw IllegalStateException(
						"Expected a boolean while deserializing a TestDocument")
				}
				stuff.add((it as JSONValue).boolean)
			}
			val allMySubs = mutableListOf<TestSubDocument>()
			json.getArray("allMySubs").forEach {
				if (!it.isObject)
				{
					throw IllegalStateException(
						"Expected an object while deserializing a TestDocument")
				}
				allMySubs.add(TestSubDocument.from(it as JSONObject))
			}
			return TestDocument(
				json.getNumber("foo").int,
				json.getNumber("bar").double,
				json.getString("baz"),
				stuff,
				allMySubs)
		}

		/**
		 * Create a new [TestDocument] from the provided bytes.
		 *
		 * @param bytes
		 *   The serialized `TestDocument`.
		 * @return
		 *   The deserialized `TestDocument`.
		 */
		fun deserialize(bytes: ByteArray): TestDocument =
			JSONReader(
				StringReader(String(bytes))).use { reader ->
				return from(reader.read() as JSONObject?
					?: error("The payload should not be empty!"))
			}

		val record0: TestDocument by lazy {
			TestDocument(
				888888,
				834.567,
				"The burden of being the first",
				listOf(true, false, true, false, true, false, true, true, true,
					false, false, false),
				listOf(TestSubDocument("Sub 0", listOf(1, 2, 3, 4))))
		}
		val record1: TestDocument by lazy {
			TestDocument(
				8,
				22.45,
				"Doc 1 Rules! ⚀",
				listOf(true, false, true, false, true, false),
				listOf(
					TestSubDocument("Sub 23", listOf(3, 4, 6)),
					TestSubDocument("Sub sdf", listOf(3, 4, 6, 9, 8))))
		}
		val record2: TestDocument by lazy {
			TestDocument(
				1342,
				1.2342345243,
				"Doc 2 is no foo!",
				listOf(true, false, false),
				listOf(
					TestSubDocument("Sub 23", listOf(3, 4, 6)),
					TestSubDocument("Sub sdf", listOf(3, 4, 6, 9, 8)),
					TestSubDocument("Sub 123423", listOf(9, 1, 2, 3, 4))))
		}
		val record3: TestDocument by lazy {
			TestDocument(
				9999999,
				1.0,
				"Breaking patterns",
				listOf(false),
				listOf(
					TestSubDocument("Sub adsf23", listOf(3, 4, 6, 8888)),
					TestSubDocument("Sub adfsdfsdf", listOf(1, 3, 4, 6, 9, 7778)),
					TestSubDocument("Sub 123423", listOf(9, 1, 2, 3, 4)),
					TestSubDocument("Sub 34233", listOf(3, 4, 6)),
					TestSubDocument("Sub sdf", listOf(3, 4, 6, 9, 8)),
					TestSubDocument("Sub 123423", listOf(9, 1, 2, 3, 4))))
		}
		val record4: TestDocument by lazy {
			TestDocument(
				1344525,
				124622.45354,
				"Doc 4 Is in the sT0r3",
				listOf(true, true),
				listOf(
					TestSubDocument(
						"Only the lonely",
						listOf(-101, -445, 3, 4))))
		}
	}
}
